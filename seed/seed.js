/**
 * TechFix — Firestore seed script
 * ---------------------------------
 * One-time admin script. NOT deployed as a Cloud Function — run it locally with Node.
 *
 * What it does:
 *  1. WIPES and re-seeds the catalog collections: branches, deviceCategories,
 *     repairServices, spareParts, technicians.
 *  2. Creates (or reuses) two demo accounts in Firebase Auth + matching `users` docs:
 *     an admin and a customer. Does NOT touch any other existing Auth users.
 *  3. Adds 3 demo repairRequests for the demo customer (pending / in_progress / completed)
 *     plus 1 matching payment doc, so My Requests / History / Receipt aren't empty on
 *     first run. This step is additive, not a wipe — safe to skip (see SEED_DEMO_REQUESTS
 *     below) if you'd rather test the real submit-request flow end to end instead.
 *
 * Field names below are taken directly from the app's model classes and repositories
 * (RepairFirestoreRepository, ResourceType, RepairRequest, Payment) — not guessed.
 *
 * Usage:
 *   1. npm install
 *   2. Place your Firebase service account key next to this file as serviceAccountKey.json
 *      (see setup steps in chat — never commit this file to git)
 *   3. node seed.js
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const PROJECT_ID = "techfix-mobile";
const keyPath = path.join(__dirname, "serviceAccountKey.json");

// Two supported ways to authenticate — use whichever you already have:
//  1. A service account key JSON at seed/serviceAccountKey.json (has a "private_key"
//     field — this is NOT the same file as app/google-services.json).
//  2. No key file at all: run `gcloud auth application-default login` once (needs the
//     Google Cloud SDK), and this falls back to those credentials automatically.
let credential;
if (fs.existsSync(keyPath)) {
  console.log(`Using service account key at ${keyPath}`);
  credential = admin.credential.cert(require(keyPath));
} else {
  console.log("No serviceAccountKey.json found — falling back to Application Default Credentials.");
  console.log("If this fails, run: gcloud auth application-default login");
  credential = admin.credential.applicationDefault();
}

admin.initializeApp({ credential, projectId: PROJECT_ID });
const db = admin.firestore();
const auth = admin.auth();

// Toggle this to false to skip step 3 (demo repairRequests + payment) entirely.
const SEED_DEMO_REQUESTS = true;

// ---------------------------------------------------------------------------
// Catalog data
// ---------------------------------------------------------------------------

const branches = [
  { id: "colombo", data: {
    name: "Colombo Branch", address: "No. 12, Galle Road, Colombo 03",
    lat: 6.9147, lng: 79.8483, contactNumber: "011 234 5678",
  }},
  { id: "galle", data: {
    name: "Galle Branch", address: "No. 5, Matara Road, Galle",
    lat: 6.0535, lng: 80.2210, contactNumber: "091 222 1111",
  }},
];

const deviceCategories = [
  { id: "phones", data: { name: "Phones" } },
  { id: "laptops", data: { name: "Laptops" } },
  { id: "tablets", data: { name: "Tablets" } },
  { id: "desktops", data: { name: "Desktops" } },
];

const repairServices = [
  { id: "screen-replacement-phone", data: {
    categoryId: "phones", name: "Screen Replacement",
    description: "OEM-grade replacement panel, calibrated and pressure-tested.",
    basePrice: 8500, estTimeHrs: 2,
  }},
  { id: "battery-service-phone", data: {
    categoryId: "phones", name: "Battery Service",
    description: "Battery health diagnostic and replacement.",
    basePrice: 4200, estTimeHrs: 1,
  }},
  { id: "charging-port-repair-phone", data: {
    categoryId: "phones", name: "Charging Port Repair",
    description: "Clean or replace a faulty charging port.",
    basePrice: 3500, estTimeHrs: 1,
  }},
  { id: "ssd-upgrade-laptop", data: {
    categoryId: "laptops", name: "SSD Upgrade",
    description: "Upgrade to solid-state storage with data migration.",
    basePrice: 12000, estTimeHrs: 3,
  }},
  { id: "ram-upgrade-laptop", data: {
    categoryId: "laptops", name: "RAM Upgrade",
    description: "Add or replace memory modules for better performance.",
    basePrice: 6500, estTimeHrs: 1,
  }},
  { id: "keyboard-replacement-laptop", data: {
    categoryId: "laptops", name: "Keyboard Replacement",
    description: "Replace a damaged or unresponsive keyboard.",
    basePrice: 7000, estTimeHrs: 2,
  }},
  { id: "screen-replacement-tablet", data: {
    categoryId: "tablets", name: "Screen Replacement",
    description: "Digitizer and display replacement for tablets.",
    basePrice: 11000, estTimeHrs: 2,
  }},
  { id: "battery-service-tablet", data: {
    categoryId: "tablets", name: "Battery Service",
    description: "Battery health check and replacement.",
    basePrice: 5500, estTimeHrs: 2,
  }},
  { id: "gpu-diagnostics-desktop", data: {
    categoryId: "desktops", name: "GPU Diagnostics & Repair",
    description: "Diagnose and repair graphics card faults.",
    basePrice: 9500, estTimeHrs: 3,
  }},
  { id: "psu-replacement-desktop", data: {
    categoryId: "desktops", name: "Power Supply Replacement",
    description: "Replace a failing or dead power supply unit.",
    basePrice: 8000, estTimeHrs: 2,
  }},
  { id: "os-reinstall-desktop", data: {
    categoryId: "desktops", name: "OS Reinstall & Tune-up",
    description: "Clean OS reinstall with driver setup and performance tune-up.",
    basePrice: 4500, estTimeHrs: 2,
  }},
];

// Two parts are deliberately out of stock at Galle — once the nearest-branch
// resolver is updated to cross-check spareParts (see findings.md), this gives
// you a real case where Colombo should win over a geographically closer branch.
const spareParts = [
  { id: "iphone-screen-colombo", data: { name: "iPhone Screen Assembly", categoryId: "phones", branchId: "colombo", stockQty: 6, unitPrice: 6500 } },
  { id: "iphone-screen-galle", data: { name: "iPhone Screen Assembly", categoryId: "phones", branchId: "galle", stockQty: 0, unitPrice: 6500 } },
  { id: "phone-battery-colombo", data: { name: "Phone Battery (Universal)", categoryId: "phones", branchId: "colombo", stockQty: 10, unitPrice: 2200 } },
  { id: "phone-battery-galle", data: { name: "Phone Battery (Universal)", categoryId: "phones", branchId: "galle", stockQty: 4, unitPrice: 2200 } },
  { id: "laptop-ssd-colombo", data: { name: "SATA/NVMe SSD 512GB", categoryId: "laptops", branchId: "colombo", stockQty: 5, unitPrice: 8500 } },
  { id: "laptop-ssd-galle", data: { name: "SATA/NVMe SSD 512GB", categoryId: "laptops", branchId: "galle", stockQty: 0, unitPrice: 8500 } },
  { id: "laptop-ram-colombo", data: { name: "DDR4 RAM 8GB", categoryId: "laptops", branchId: "colombo", stockQty: 12, unitPrice: 4200 } },
  { id: "laptop-keyboard-galle", data: { name: "Laptop Keyboard (Generic)", categoryId: "laptops", branchId: "galle", stockQty: 3, unitPrice: 3800 } },
  { id: "tablet-screen-colombo", data: { name: "Tablet Digitizer Panel", categoryId: "tablets", branchId: "colombo", stockQty: 4, unitPrice: 7200 } },
  { id: "desktop-psu-colombo", data: { name: "ATX Power Supply 550W", categoryId: "desktops", branchId: "colombo", stockQty: 6, unitPrice: 5200 } },
];

const technicians = [
  { id: "kasun-silva", data: { name: "Kasun Silva", branchId: "colombo", specialization: "Screens & Digitizers", isAvailable: true } },
  { id: "chamara-fernando", data: { name: "Chamara Fernando", branchId: "colombo", specialization: "Laptop & PC Hardware", isAvailable: false } },
  { id: "dilani-wickrama", data: { name: "Dilani Wickramasinghe", branchId: "colombo", specialization: "Batteries & Power", isAvailable: true } },
  { id: "nimal-perera", data: { name: "Nimal Perera", branchId: "galle", specialization: "General Repair", isAvailable: true } },
  { id: "waruni-jayasuriya", data: { name: "Waruni Jayasuriya", branchId: "galle", specialization: "Screens & Digitizers", isAvailable: false } },
];

// role "customer" is what RegisterActivity writes by default — kept identical here.
const demoUsers = [
  { email: "admin@techfix.lk", password: "Admin@2026", name: "TechFix Admin", phone: "011 200 0000", role: "admin" },
  { email: "nadeesha@mail.com", password: "Customer@2026", name: "Nadeesha Perera", phone: "077 123 4567", role: "customer" },
];

const DAY = 24 * 60 * 60 * 1000;

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

async function wipeCollection(name) {
  const snap = await db.collection(name).get();
  if (snap.empty) return;
  const batch = db.batch();
  snap.docs.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  console.log(`  wiped ${snap.size} existing doc(s) from ${name}`);
}

async function seedCollection(name, docs) {
  const batch = db.batch();
  docs.forEach(({ id, data }) => batch.set(db.collection(name).doc(id), data));
  await batch.commit();
  console.log(`  seeded ${docs.length} doc(s) into ${name}`);
}

async function upsertAuthUser({ email, password, name }) {
  try {
    const user = await auth.createUser({ email, password, displayName: name });
    console.log(`  created auth user ${email}`);
    return user;
  } catch (e) {
    if (e.code === "auth/email-already-exists") {
      console.log(`  auth user ${email} already exists, reusing`);
      return auth.getUserByEmail(email);
    }
    throw e;
  }
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main() {
  console.log("Wiping catalog collections (branches, deviceCategories, repairServices, spareParts, technicians)...");
  for (const c of ["branches", "deviceCategories", "repairServices", "spareParts", "technicians"]) {
    await wipeCollection(c);
  }

  console.log("Seeding catalog data...");
  await seedCollection("branches", branches);
  await seedCollection("deviceCategories", deviceCategories);
  await seedCollection("repairServices", repairServices);
  await seedCollection("spareParts", spareParts);
  await seedCollection("technicians", technicians);

  console.log("Creating demo accounts...");
  const uidByEmail = {};
  for (const u of demoUsers) {
    const userRecord = await upsertAuthUser(u);
    uidByEmail[u.email] = userRecord.uid;
    await db.collection("users").doc(userRecord.uid).set({
      uid: userRecord.uid, name: u.name, email: u.email, phone: u.phone, role: u.role,
    });
  }

  if (SEED_DEMO_REQUESTS) {
    console.log("Seeding demo repair requests + payment for the demo customer...");
    const customerUid = uidByEmail["nadeesha@mail.com"];
    const now = Date.now();

    const demoRequests = [
      { id: "demo-req-1", data: {
        customerId: customerUid, serviceId: "screen-replacement-phone", categoryId: "phones",
        deviceDetails: "iPhone 12, cracked front glass", issueDesc: "Touch unresponsive in top-right corner",
        devicePhotoUrl: "", assignedBranchId: "colombo", assignedTechnicianId: "kasun-silva",
        status: "in_progress", customerLat: 6.9147, customerLng: 79.8483,
        requestedAt: now - 5 * DAY, completedAt: 0,
      }},
      { id: "demo-req-2", data: {
        customerId: customerUid, serviceId: "battery-service-phone", categoryId: "phones",
        deviceDetails: "Samsung Galaxy S21, battery drains fast", issueDesc: "Drops from 100% to 20% in about two hours",
        devicePhotoUrl: "", assignedBranchId: "colombo", assignedTechnicianId: null,
        status: "pending", customerLat: 6.9147, customerLng: 79.8483,
        requestedAt: now - 1 * DAY, completedAt: 0,
      }},
      { id: "demo-req-3", data: {
        customerId: customerUid, serviceId: "ssd-upgrade-laptop", categoryId: "laptops",
        deviceDetails: "Dell XPS 13, slow boot times", issueDesc: "Upgrade from HDD to SSD requested",
        devicePhotoUrl: "", assignedBranchId: "colombo", assignedTechnicianId: "chamara-fernando",
        status: "completed", customerLat: 6.9147, customerLng: 79.8483,
        requestedAt: now - 14 * DAY, completedAt: now - 10 * DAY,
        paymentStatus: "completed", // written by the payhereNotify webhook in the real flow
      }},
    ];
    await seedCollection("repairRequests", demoRequests);

    await db.collection("payments").doc("demo-pay-1").set({
      requestId: "demo-req-3", customerId: customerUid, amount: 12000, currency: "LKR",
      status: "completed", payherePaymentId: "DEMO12345", method: "card",
      // paidAt must be a real Firestore Timestamp — PaymentRepository/RequestSyncManager
      // both call doc.getTimestamp("paidAt"), a plain number here would read back as null.
      paidAt: admin.firestore.Timestamp.fromMillis(now - 10 * DAY),
    });
    console.log("  seeded 3 repairRequests + 1 payment");
  }

  console.log("\nDone. Demo logins:");
  demoUsers.forEach((u) => console.log(`  ${u.role.padEnd(8)} ${u.email} / ${u.password}`));
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("Seed failed:", err);
    process.exit(1);
  });
