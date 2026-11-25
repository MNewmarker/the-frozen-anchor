# ⚓ The Frozen Anchor

> **Built for Techs, by a Tech.**  
> An offline-first Pressure/Temperature (P/T) chart, superheat, and subcooling utility engineered for HVAC/R field technicians working in zero-connectivity environments.
>
> 🔗 **Launch the Live App:** [https://netlify.app](https://netlify.app)

---

## 📱 How to Install on Your Phone (100% Offline)

1. **iPhone (Safari):** Open [thefrozenanchor.netlify.app](https://netlify.app) in **Safari**, tap the **Share** icon (`↑`), and select **Add to Home Screen**.
2. **Android (Chrome):** Open [thefrozenanchor.netlify.app](https://netlify.app) in **Chrome**, tap the **Menu (`⋮`)**, and select **Install App** (or **Add to Home screen**).
3. **Cache Once:** Open the app once while connected to Wi-Fi or cell service to cache all 34 refrigerant datasets—after that, it runs completely offline in Airplane Mode.

---

## 📋 Overview

**The Frozen Anchor** solves a common operational problem in marine and commercial HVAC/R: technicians frequently work inside steel engine rooms, below-deck machinery spaces, and mechanical basements where cellular and Wi-Fi signals are completely unavailable.

Designed and built by a **Retired U.S. Coast Guard Chief Machinery Technician (MKC)** and current **Computer Science student (4.0 GPA)**, this project translates real-world refrigeration thermodynamics into a clean, object-oriented software architecture.

### Key Features
* **34 NIST/ASHRAE-Verified Refrigerants:** Includes pure fluids, zeotropic/azeotropic blends (ASHRAE 400 & 500 series), hydrocarbons, A2Ls (`R-32`, `R-454B`, `R-452B`, `R-1234yf`, `R-1234ze`), and transcritical CO₂ (`R-744`).
* **High-Accuracy Tabular Lookup (~0.2°F Variance):** Parses local TSV thermodynamic tables using a minimum-difference search algorithm to resolve saturation values without relying on cloud APIs.
* **Bidirectional Lookup & Field Controls:** Resolve saturation temperature from gauge pressure (`PSIG` / `in. Hg vac.`) or saturation pressure from temperature (`°F` / `°C`), featuring a dedicated **`± VAC`** toggle for quick negative vacuum entries.
* **Thermodynamically Accurate Superheat & Subcooling:** Automatically applies **Dew Point** for superheat and **Bubble Point** for subcooling on blended refrigerants exhibiting temperature glide, complete with probe clamp-location reminders.
* **Offline-First Progressive Web App (PWA):** Zero network dependency once cached, featuring a high-contrast dark theme designed for low-light mechanical spaces.

---

## 🏗️ Software Architecture & OOP Design

The core calculation engine was originally architected in **Java** using strict Object-Oriented Programming principles and subsequently ported 1-to-1 into **JavaScript** to power the mobile-friendly PWA front end.

### Class Hierarchy & Polymorphism
* **`Refrigerant` (Abstract Base Class):** Encapsulates shared thermodynamic state (`psig`, `tempFahrenheit`, `tempCelsius`, `lookupByPressure`), unit conversions, and enforces the contract for `setPT()`, `calculateSuperheat()`, and `calculateSubcooling()`.
* **`PureRefrigerant` (Subclass):** Handles single-component and negligible-glide fluids (e.g., `R-22`, `R-134a`, `R-32`) using a two-column TSV saturation curve (`Temp °F | PSIG`).
* **`BlendedRefrigerant` (Subclass):** Handles ASHRAE 400 and 500 series blends (e.g., `R-410A`, `R-404A`, `R-407C`, `R-454B`) using a three-column TSV table (`PSIG | Bubble °F | Dew °F`) and overrides superheat and subcooling calculations to account for temperature glide.
* **`Superheat` & `SubCooling` (Service Layer):** Decoupled service handlers that accept any `Refrigerant` reference polymorphically and compute temperature differentials (Δ T) in both Fahrenheit and Celsius.

### 🔄 Development Workflow: From Java Engine to Offline PWA
1. **Core Architecture (Java):** Designed and wrote the original Object-Oriented calculation engine, class hierarchy (`Refrigerant`, `PureRefrigerant`, `BlendedRefrigerant`), polymorphic service handlers (`Superheat`, `SubCooling`), and TSV parsing logic in Java.
2. **Thermodynamic Data Verification:** Compiled and audited all 34 refrigerant saturation tables against NIST/ASHRAE benchmarks.
3. **AI-Assisted PWA Port:** Leveraged AI tooling to accelerate porting the 1-to-1 Java OOP architecture into vanilla JavaScript, HTML5, CSS3, and a Service Worker (`sw.js`) for offline mobile deployment—followed by field-focused UX refinements (such as the `± VAC` toggle and low-light contrast).

---

## 📂 Repository Structure

```text
the-frozen-anchor/
├── java-core/                         # Original Java CLI & OOP Engine
│   └── src/main/java/com/thefrozenanchor/
│       ├── Main.java                  # CLI entry point & polymorphic router
│       ├── model/
│       │   ├── Refrigerant.java       # Abstract base class
│       │   ├── PureRefrigerant.java   # Single-component implementation
│       │   └── BlendedRefrigerant.java# Bubble/Dew point blend implementation
│       └── service/
│           ├── Superheat.java         # Evaporator superheat calculator
│           └── SubCooling.java        # Condenser subcooling calculator
├── pwa/                               # Progressive Web App (HTML/CSS/JS)
│   ├── index.html                     # Mobile-first dark-mode UI
│   ├── app.js                         # 1-to-1 JS OOP port & DOM controller
│   ├── manifest.json                  # PWA install manifest
│   ├── sw.js                          # Offline Service Worker cache
│   └── main/resources/refrigerants/   # 34 audited TSV thermodynamic lookup tables
└── README.md
```

---

## 🚀 Roadmap (v2 & Beyond)

* [x] **v1.0:** Core Java OOP engine & offline-capable JavaScript PWA with 34 verified refrigerants.
* [ ] **v1.1:** Automated JUnit test suite verifying thermodynamic accuracy across all 34 TSV datasets.
* [ ] **v2.0 – Chief's Playbook:** Quick-reference field guides, charging best practices, and top-ten diagnostic checks for junior technicians.
* [ ] **v2.1 – System Troubleshooting Tree:** Interactive fault-isolation workflows for common refrigeration and air conditioning failures.
* [ ] **v3.0:** Native iOS and Android mobile deployment.

---

## ⚠️ Disclaimer & Safety Notice

This application is intended strictly as a general reference and educational aid for qualified HVAC/R professionals. It does not replace proper EPA certification, formal training, or adherence to manufacturer service specifications and local safety codes—especially when servicing systems containing flammable (A2L/A3) or high-pressure transcritical refrigerants.

---

## 👤 Author

**Michael Newmarker**  
Retired Chief Machinery Technician (MKC), U.S. Coast Guard | Former MARMC GS-12  
AC&R / HVAC Trades Instructor | Computer Science Student (4.0 GPA, Class of Summer 2027)

* [GitHub Profile](https://github.com)
* [LinkedIn Profile](https://linkedin.com)

### Feedback & Bug Reports
Testing this on a job site? Spot a UI quirk or want to suggest a v2 feature? Open an **Issue** right here on GitHub!

## 👤 Author

**[Michael Newmarker]**
Retired Chief Machinery Technician (MKC), U.S. Coast Guard
Computer Science Student (4.0 GPA)
* https://www.linkedin.com/in/michaelnewmarker-computerscience/