const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const crypto = require("crypto");

initializeApp();
const db = getFirestore();

const payhereSecret = defineSecret("PAYHERE_MERCHANT_SECRET");

function md5(input) {
  return crypto.createHash("md5").update(input).digest("hex");
}

exports.payhereNotify = onRequest(
  {
    secrets: [payhereSecret],
  },
  async (req, res) => {
    try {
      const {
        merchant_id,
        order_id,
        payment_id,
        payhere_amount,
        payhere_currency,
        status_code,
        md5sig,
      } = req.body;

      if (
        !merchant_id ||
        !order_id ||
        !payhere_amount ||
        !payhere_currency ||
        !status_code ||
        !md5sig
      ) {
        return res.status(400).send("Missing required fields");
      }

      const merchantSecret = payhereSecret.value();
      const localSig = md5(
        merchant_id +
        order_id +
        payhere_amount +
        payhere_currency +
        status_code +
        md5(merchantSecret).toUpperCase()
      ).toUpperCase();

      if (localSig !== md5sig.toUpperCase()) {
        console.warn("Invalid PayHere signature for order:", order_id);
        return res.status(400).send("Invalid signature");
      }

      const status = status_code === "2" ? "completed" : "failed";
      const paymentRef = db.collection("payments").doc(order_id);

      await paymentRef.update({
        status: status,
        payherePaymentId: payment_id || null,
        paidAt: FieldValue.serverTimestamp(),
      });

      const paymentDoc = await paymentRef.get();
      if (paymentDoc.exists) {
        const requestId = paymentDoc.data().requestId;
        if (requestId) {
          await db.collection("repairRequests").doc(requestId).update({
            paymentStatus: status,
          });
        }
      }

      return res.status(200).send("OK");
    } catch (error) {
      console.error("PayHere notification error:", error);
      return res.status(500).send("Internal server error");
    }
  }
);