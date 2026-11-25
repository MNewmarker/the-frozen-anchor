/**
 * @fileoverview Client-side OOP calculation engine and UI controller for The Frozen Anchor PWA.
 * @author Michael Newmarker, Retired USCG MKC
 */

if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
        navigator.serviceWorker.register("./sw.js").catch(() => {});
    });
}

const REFRIGERANT_LIST = [
    { label: "R-11", file: "R11.tsv" },
    { label: "R-12", file: "R12.tsv" },
    { label: "R-22", file: "R22.tsv" },
    { label: "R-23", file: "r23.tsv" },
    { label: "R-32", file: "R32.tsv" },
    { label: "R-114", file: "R114.tsv" },
    { label: "R-123", file: "R123.tsv" },
    { label: "R-124", file: "R124.tsv" },
    { label: "R-134a", file: "R134A.tsv" },
    { label: "R-245fa", file: "R245FA.tsv" },
    { label: "R-290 (Propane)", file: "R290.tsv" },
    { label: "R-404A", file: "R404A.tsv" },
    { label: "R-407A", file: "R407A.tsv" },
    { label: "R-407C", file: "R407C.tsv" },
    { label: "R-407F", file: "R407F.tsv" },
    { label: "R-410A", file: "R410A.tsv" },
    { label: "R-422B", file: "R422B.tsv" },
    { label: "R-422D", file: "R422D.tsv" },
    { label: "R-438A", file: "R438A.tsv" },
    { label: "R-448A", file: "R448A.tsv" },
    { label: "R-449A", file: "R449A.tsv" },
    { label: "R-452A", file: "R452A.tsv" },
    { label: "R-452B", file: "R452B.tsv" },
    { label: "R-454A", file: "R454A.tsv" },
    { label: "R-454B", file: "R454B.tsv" },
    { label: "R-454C", file: "R454C.tsv" },
    { label: "R-502", file: "R502.tsv" },
    { label: "R-507A", file: "R507A.tsv" },
    { label: "R-513A", file: "R513A.tsv" },
    { label: "R-600a (Isobutane)", file: "R600A.tsv" },
    { label: "R-744 (CO2)", file: "R744.tsv" },
    { label: "R-1234yf", file: "R1234YF.tsv" },
    { label: "R-1234ze", file: "R1234ZE.tsv" },
    { label: "R-1270 (Propylene)", file: "R1270.tsv" }
];

const fileCache = new Map();
class Refrigerant {
    constructor() {
        this.refrigerantName = "";
        this.fileName = "";
        this.psig = 0.0;
        this.tempFahrenheit = 0.0;
        this.tempCelsius = 0.0;
        this.lookupByPressure = true;
    }
    setRefrigerant(name, fileName) {
        this.refrigerantName = name;
        this.fileName = fileName;
    }
    setPsig(psig) {
        this.psig = psig;
        this.lookupByPressure = true;
    }
    setTemp(tempF) {
        this.tempFahrenheit = tempF;
        this.tempCelsius = this.convertFtoC(tempF);
        this.lookupByPressure = false;
    }
    convertFtoC(tempF) {
        return (tempF - 32.0) / 1.8;
    }
    async loadRows() {
        if (fileCache.has(this.fileName)) return fileCache.get(this.fileName);
        const res = await fetch(`main/resources/refrigerants/${this.fileName}`);
        if (!res.ok) throw new Error("File not found");
        const text = await res.text();
        const rows = text
            .trim()
            .split(/\r?\n/)
            .map(line => line.trim().split(/\s+/).map(Number))
            .filter(cols => cols.length >= 2 && !isNaN(cols.at(0)) && !isNaN(cols.at(1)));
        fileCache.set(this.fileName, rows);
        return rows;
    }
}

class PureRefrigerant extends Refrigerant {
    async setPT() {
        const rows = await this.loadRows();
        let smallestDiff = Number.MAX_VALUE;
        let bestTempF = 0.0;
        let bestPsig = 0.0;

        for (const row of rows) {
            const rowTempF = row.at(0);
            const rowPsig = row.at(1);
            const currentDiff = this.lookupByPressure
                ? Math.abs(this.psig - rowPsig)
                : Math.abs(this.tempFahrenheit - rowTempF);

            if (currentDiff < smallestDiff) {
                smallestDiff = currentDiff;
                bestTempF = rowTempF;
                bestPsig = rowPsig;
            }
        }
        this.psig = bestPsig;
        this.tempFahrenheit = bestTempF;
        this.tempCelsius = this.convertFtoC(bestTempF);
    }
    calculateSuperheat(suctionLineTemp) {
        return suctionLineTemp - this.tempFahrenheit;
    }
    calculateSubcooling(liquidLineTemp) {
        return this.tempFahrenheit - liquidLineTemp;
    }
}

class BlendedRefrigerant extends Refrigerant {
    constructor() {
        super();
        this.bubbleTempF = 0.0;
        this.dewTempF = 0.0;
        this.bubbleTempC = 0.0;
        this.dewTempC = 0.0;
    }
    async setPT() {
        const rows = await this.loadRows();
        let smallestDiff = Number.MAX_VALUE;
        let bestPsig = 0.0;
        let bestBubbleF = 0.0;
        let bestDewF = 0.0;

        for (const row of rows) {
            const rowPsig = row.at(0);
            const rowBubbleF = row.at(1);
            const rowDewF = row.at(2);
            const currentDiff = this.lookupByPressure
                ? Math.abs(this.psig - rowPsig)
                : Math.abs(this.tempFahrenheit - rowBubbleF);

            if (currentDiff < smallestDiff) {
                smallestDiff = currentDiff;
                bestPsig = rowPsig;
                bestBubbleF = rowBubbleF;
                bestDewF = rowDewF;
            }
        }
        this.psig = bestPsig;
        this.bubbleTempF = bestBubbleF;
        this.dewTempF = bestDewF;
        this.bubbleTempC = this.convertFtoC(bestBubbleF);
        this.dewTempC = this.convertFtoC(bestDewF);
        this.tempFahrenheit = bestBubbleF;
        this.tempCelsius = this.bubbleTempC;
    }
    calculateSuperheat(suctionLineTemp) {
        return suctionLineTemp - this.dewTempF;
    }
    calculateSubcooling(liquidLineTemp) {
        return this.bubbleTempF - liquidLineTemp;
    }
}
let lookupMode = "pressure";
let sideChoice = 1;
let lookupUnit = "F";
let lineUnit = "F";
let hvac1 = null;
let bannerTimer = null;

function showBanner(msg, isInfo = false) {
    const banner = document.getElementById("statusBanner");
    banner.textContent = msg;
    banner.className = isInfo ? "banner info" : "banner";
    banner.style.display = "block";
    clearTimeout(bannerTimer);
    bannerTimer = setTimeout(() => {
        banner.style.display = "none";
    }, 3500);
}

function hideBanner() {
    document.getElementById("statusBanner").style.display = "none";
}

function parseNumericInput(inputEl) {
    const cleaned = inputEl.value.trim().replace(/[^\d.-]/g, "");
    if (cleaned === "" || cleaned === "-" || cleaned === ".") return NaN;
    return Number(cleaned);
}

const refSelect = document.getElementById("refSelect");
REFRIGERANT_LIST.forEach((item, idx) => {
    const opt = document.createElement("option");
    opt.value = String(idx);
    opt.textContent = item.label;
    if (item.file === "R410A.tsv") opt.selected = true;
    refSelect.appendChild(opt);
});

refSelect.addEventListener("change", () => {
    hideBanner();
    document.getElementById("resultsCard").style.display = "none";
});

document.getElementById("beginBtn").addEventListener("click", () => {
    document.getElementById("disclaimerScreen").style.display = "none";
    document.getElementById("menuScreen").style.display = "block";
});

document.getElementById("openPtBtn").addEventListener("click", () => {
    hideBanner();
    document.getElementById("menuScreen").style.display = "none";
    document.getElementById("ptScreen").style.display = "block";
});

document.getElementById("playbookBtn").addEventListener("click", () => {
    showBanner("Chief's Playbook — Coming in v2!", true);
});

document.getElementById("troubleshootBtn").addEventListener("click", () => {
    showBanner("Full System Troubleshooting Tree — Coming in v2!", true);
});

document.getElementById("backToMenuBtn").addEventListener("click", () => {
    hideBanner();
    document.getElementById("ptScreen").style.display = "none";
    document.getElementById("resultsCard").style.display = "none";
    document.getElementById("menuScreen").style.display = "block";
});


document.getElementById("toggleNegPressBtn").addEventListener("click", () => {
    const input = document.getElementById("pressureInput");
    if (!input.value) {
        input.value = "-";
    } else if (input.value.startsWith("-")) {
        input.value = input.value.slice(1);
    } else {
        input.value = "-" + input.value;
    }
    input.focus();
});
const modePressBtn = document.getElementById("modePressBtn");
const modeTempBtn = document.getElementById("modeTempBtn");
modePressBtn.addEventListener("click", () => {
    lookupMode = "pressure";
    modePressBtn.classList.add("active");
    modeTempBtn.classList.remove("active");
    document.getElementById("pressureSection").style.display = "block";
    document.getElementById("tempSection").style.display = "none";
    document.getElementById("resultsCard").style.display = "none";
    hideBanner();
});

modeTempBtn.addEventListener("click", () => {
    lookupMode = "temp";
    modeTempBtn.classList.add("active");
    modePressBtn.classList.remove("active");
    document.getElementById("pressureSection").style.display = "none";
    document.getElementById("tempSection").style.display = "block";
    document.getElementById("resultsCard").style.display = "none";
    hideBanner();
});

const sideHighBtn = document.getElementById("sideHighBtn");
const sideLowBtn = document.getElementById("sideLowBtn");
sideHighBtn.addEventListener("click", () => {
    sideChoice = 1;
    sideHighBtn.classList.add("active");
    sideLowBtn.classList.remove("active");
    document.getElementById("resultsCard").style.display = "none";
    hideBanner();
});

sideLowBtn.addEventListener("click", () => {
    sideChoice = 2;
    sideLowBtn.classList.add("active");
    sideHighBtn.classList.remove("active");
    document.getElementById("resultsCard").style.display = "none";
    hideBanner();
});

const lookupFBtn = document.getElementById("lookupFBtn");
const lookupCBtn = document.getElementById("lookupCBtn");
lookupFBtn.addEventListener("click", () => {
    lookupUnit = "F";
    lookupFBtn.classList.add("active");
    lookupCBtn.classList.remove("active");
    document.getElementById("tempInputLabel").textContent = "SATURATION TEMPERATURE (°F)";
});

lookupCBtn.addEventListener("click", () => {
    lookupUnit = "C";
    lookupCBtn.classList.add("active");
    lookupFBtn.classList.remove("active");
    document.getElementById("tempInputLabel").textContent = "SATURATION TEMPERATURE (°C)";
});
async function runPtLookup() {
    hideBanner();
    const selected = REFRIGERANT_LIST.at(Number(refSelect.value));
    const digits = selected.label.replace(/\D/g, "");
    const firstDigit = digits.length > 0 ? digits.charAt(0) : "0";

    if (firstDigit === "4" || firstDigit === "5") {
        hvac1 = new BlendedRefrigerant();
    } else {
        hvac1 = new PureRefrigerant();
    }
    hvac1.setRefrigerant(selected.label, selected.file);

    if (lookupMode === "pressure") {
        const pressInput = document.getElementById("pressureInput");
        const pVal = parseNumericInput(pressInput);
        if (isNaN(pVal)) {
            showBanner("Please enter a valid pressure reading.");
            return;
        }
        pressInput.blur();
        hvac1.setPsig(pVal);
    } else {
        const tempInput = document.getElementById("tempInput");
        const tVal = parseNumericInput(tempInput);
        if (isNaN(tVal)) {
            showBanner("Please enter a valid temperature reading.");
            return;
        }
        tempInput.blur();
        const tempF = lookupUnit === "F" ? tVal : (tVal * 1.8) + 32.0;
        hvac1.setTemp(tempF);
    }

    try {
        await hvac1.setPT();
    } catch (err) {
        showBanner("Refrigerant data file not found or unreadable.");
        return;
    }

    const satGrid = document.getElementById("satGrid");
    if (hvac1 instanceof BlendedRefrigerant) {
        satGrid.style.gridTemplateColumns = "1fr 1fr";
        satGrid.innerHTML = `
            <div class="stat" style="grid-column: span 2;">
                <span>SATURATION PRESSURE</span>
                <strong>${hvac1.psig.toFixed(1)} PSIG</strong>
            </div>
            <div class="stat">
                <span>BUBBLE TEMP (Liquid)</span>
                <strong>${hvac1.bubbleTempF.toFixed(1)} °F</strong>
                <small>${hvac1.bubbleTempC.toFixed(1)} °C</small>
            </div>
            <div class="stat">
                <span>DEW TEMP (Vapor)</span>
                <strong>${hvac1.dewTempF.toFixed(1)} °F</strong>
                <small>${hvac1.dewTempC.toFixed(1)} °C</small>
            </div>
        `;
    } else {
        satGrid.style.gridTemplateColumns = "1fr 1fr";
        satGrid.innerHTML = `
            <div class="stat">
                <span>SATURATION PRESSURE</span>
                <strong>${hvac1.psig.toFixed(1)} PSIG</strong>
            </div>
            <div class="stat">
                <span>SATURATION TEMP</span>
                <strong>${hvac1.tempFahrenheit.toFixed(1)} °F</strong>
                <small>${hvac1.tempCelsius.toFixed(1)} °C</small>
            </div>
        `;
    }

    const resultsCard = document.getElementById("resultsCard");
    resultsCard.style.display = "block";
    const deltaSection = document.getElementById("deltaSection");
    const deltaInputArea = document.getElementById("deltaInputArea");
    const deltaBox = document.getElementById("deltaBox");

    if (lookupMode === "pressure") {
        deltaSection.style.display = "block";
        deltaInputArea.style.display = "none";
        deltaBox.style.display = "none";
        document.getElementById("deltaYesBtn").classList.remove("active");
        document.getElementById("deltaNoBtn").classList.add("active");

        if (sideChoice === 1) {
            document.getElementById("deltaPromptLabel").textContent = "WOULD YOU LIKE TO CALCULATE SUBCOOLING?";
            document.getElementById("fieldReminder").textContent = "Measure liquid line temp leaving the condenser (use Bubble Temp for blends).";
            document.getElementById("deltaCalcBtn").textContent = "CALCULATE SUBCOOLING";
            updateLineTempLabel();
        } else {
            document.getElementById("deltaPromptLabel").textContent = "WOULD YOU LIKE TO CALCULATE SUPERHEAT?";
            document.getElementById("fieldReminder").textContent = "Measure suction line temp 6\" from compressor/evaporator (use Dew Temp for blends).";
            document.getElementById("deltaCalcBtn").textContent = "CALCULATE SUPERHEAT";
            updateLineTempLabel();
        }
    } else {
        deltaSection.style.display = "none";
    }

    resultsCard.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

document.getElementById("ptCalcBtn").addEventListener("click", runPtLookup);

["pressureInput", "tempInput"].forEach((id) => {
    document.getElementById(id).addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            runPtLookup();
        }
    });
});

function updateLineTempLabel() {
    const type = sideChoice === 1 ? "LIQUID" : "SUCTION";
    document.getElementById("lineTempLabel").textContent = `ACTUAL ${type} LINE TEMPERATURE (°${lineUnit})`;
}

document.getElementById("deltaYesBtn").addEventListener("click", () => {
    document.getElementById("deltaYesBtn").classList.add("active");
    document.getElementById("deltaNoBtn").classList.remove("active");
    const area = document.getElementById("deltaInputArea");
    area.style.display = "block";
    area.scrollIntoView({ behavior: "smooth", block: "nearest" });
});

document.getElementById("deltaNoBtn").addEventListener("click", () => {
    document.getElementById("deltaNoBtn").classList.add("active");
    document.getElementById("deltaYesBtn").classList.remove("active");
    document.getElementById("deltaInputArea").style.display = "none";
    document.getElementById("deltaBox").style.display = "none";
});

document.getElementById("lineFBtn").addEventListener("click", () => {
    lineUnit = "F";
    document.getElementById("lineFBtn").classList.add("active");
    document.getElementById("lineCBtn").classList.remove("active");
    updateLineTempLabel();
});

document.getElementById("lineCBtn").addEventListener("click", () => {
    lineUnit = "C";
    document.getElementById("lineCBtn").classList.add("active");
    document.getElementById("lineFBtn").classList.remove("active");
    updateLineTempLabel();
});

function runDeltaCalc() {
    if (!hvac1) return;
    hideBanner();
    const lineInput = document.getElementById("lineTempInput");
    const rawLine = parseNumericInput(lineInput);
    if (isNaN(rawLine)) {
        showBanner("Please enter a valid line temperature.");
        return;
    }
    lineInput.blur();
    const lineTempF = lineUnit === "F" ? rawLine : (rawLine * 1.8) + 32.0;
    const deltaBox = document.getElementById("deltaBox");

    if (sideChoice === 1) {
        const scF = hvac1.calculateSubcooling(lineTempF);
        const scC = scF / 1.8;
        deltaBox.innerHTML = `
            <span>SUBCOOLING</span>
            <strong>${scF.toFixed(1)} °F (${scC.toFixed(1)} °C)</strong>
        `;
    } else {
        const shF = hvac1.calculateSuperheat(lineTempF);
        const shC = shF / 1.8;
        deltaBox.innerHTML = `
            <span>SUPERHEAT</span>
            <strong>${shF.toFixed(1)} °F (${shC.toFixed(1)} °C)</strong>
        `;
    }
    deltaBox.style.display = "block";
    deltaBox.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

document.getElementById("deltaCalcBtn").addEventListener("click", runDeltaCalc);
document.getElementById("lineTempInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        e.preventDefault();
        runDeltaCalc();
    }
});
