# Claude Conversation Log

Session ID: agent-a443a13
Date: 2025-12-20 18:57:35

---

## 👤 User

Explore the codebase to find what kinetic equation type each Metabolizer uses. Look for:
1. Each Metabolizer class/implementation
2. The rate equations they use (zero order, first order, Michaelis-Menten, Hill equation, etc.)
3. The specific metabolites each processes (inputs and outputs)

Search through the metabolizer implementations and ODE files to identify the mathematical models used. Return a structured list of: Metabolizer name -> metabolites processed -> equation type.

---

## 🤖 Claude

I'll help you explore the codebase to identify each Metabolizer's kinetic equations, metabolites, and equation types. Let me start by understanding the project structure.

---

## 🤖 Claude

Let me check the actual structure of the project:

---

## 🤖 Claude

This is a Kotlin/Gradle project. Let me search for Metabolizer implementations:

---

## 🤖 Claude

Now let me search for the Metabolizer interface and implementations:

---

## 🤖 Claude

Good. Now let me search for all Metabolizer implementations by looking for classes that extend Metabolizer:

---

## 🤖 Claude

Let me search for ODE implementations and enzymatic processes:

---

## 🤖 Claude

Great! I found several metabolizers. Let me continue searching and get more:

---

## 🤖 Claude

Perfect! Now let me read each of these metabolizer implementations to extract the kinetic equations and metabolites:

---

## 🤖 Claude

Now let me read the remaining metabolizers:

---

## 🤖 Claude

Let me continue reading more metabolizers:

---

## 🤖 Claude

Let me continue with the remaining metabolizers:

---

## 🤖 Claude

Let me read the PepsinODE and the remaining metabolizers:

---

## 🤖 Claude

Let me check a few more files I haven't read yet:

---

## 🤖 Claude

Let me check the EtcODE as well:

---

## 🤖 Claude

Excellent! Now I have comprehensive information about all the metabolizers. Let me compile the structured report:

## Metabolizer Analysis Report

Based on my exploration of the codebase, here is a comprehensive structured list of all Metabolizers with their kinetic equation types, metabolites processed, and equation classification:

---

### **BLOOD COMPARTMENT**

**1. GlucoseUptakeRegulator**
- **Metabolites Processed**: INSULIN (input) → GLUCOSE (output consumed)
- **Equation Type**: Modified Michaelis-Menten with Hill equation kinetics
- **Mathematical Form**: Glucose uptake rate with Hill-type insulin regulation (γ=1.62 Hill coefficient)
- **Details**: v_max depends on insulin concentration using Hill equation; then glucose consumption follows MM kinetics
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/blood/GlucoseUptakeRegulator.kt`

**2. InsulinBreakdown**
- **Metabolites Processed**: INSULIN → (degradation)
- **Equation Type**: First-order (exponential decay)
- **Mathematical Form**: d[Insulin]/dt = -k[Insulin], where k = ln(2)/4 (half-life = 4 minutes)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/blood/InsulinBreakdown.kt`

**3. LipoproteinLipase**
- **Metabolites Processed**: CHYLOMICRON → CHYLOMICRON_REMNANT + OLEIC_ACID + GLYCEROL
- **Equation Type**: Zero-order (linear mass action)
- **Mathematical Form**: Simple first-order linear degradation with stoichiometric conversions (rates: 0.1, 10x, 1/3x)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/blood/LipoproteinLipase.kt`

---

### **CELLULAR METABOLISM**

**4. Glycolysis**
- **Metabolites Processed**: GLUCOSE → G6P → F6P → F16BP → e3PG → PEP → PYRUVATE (7 intermediates)
- **Equation Type**: Complex multi-enzyme Michaelis-Menten with allosteric regulation
- **Mathematical Form**: Multiple MM kinetics with Hill equation for PFK (n=4); allosteric activation of pyruvate kinase by F16BP
- **Key Regulatory Elements**:
  - Glucose Transporter: MM kinetics
  - Hexokinase: MM kinetics
  - Phosphofructokinase: Hill equation (n=4, cooperative binding)
  - Pyruvate Kinase: Allosteric activation by F16BP reducing Km
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/GlycolysisODE.kt`

**5. AcylCoASynthase**
- **Metabolites Processed**: OLEIC_ACID → FATTY_ACYL_COA
- **Equation Type**: First-order linear
- **Mathematical Form**: Simple first-order kinetics (rate constant = 0.5)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/AcylCoASynthase.kt`

---

### **MITOCHONDRIAL METABOLISM**

**6. TCACycle (Citric Acid Cycle)**
- **Metabolites Processed**: PYRUVATE + KETOGLUTARATE + ACETYL_COA → NADH + FADH2 + GTP
- **Equation Type**: First-order linear with stoichiometric production
- **Mathematical Form**: Linear consumption (75% per minute) with multi-product formation (3 NADH per pyruvate, 2 per α-KG, 3 per Acetyl-CoA; 1 FADH2 per each substrate)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/mitochondria/TcaODE.kt`

**7. ElectronTransportChain**
- **Metabolites Processed**: NADH + FADH2 → ATP
- **Equation Type**: Michaelis-Menten (saturation-based with limiting substrates)
- **Mathematical Form**: Fixed ATP production capacity with competitive utilization by NADH and FADH2
- **Details**: 2.5 ATP per NADH, 1.5 ATP per FADH2; substrate-limited when supply exceeds 6000 mmol ATP/min capacity
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/mitochondria/EtcODE.kt`

**8. FattyAcidOxidation**
- **Metabolites Processed**: FATTY_ACYL_COA → ACETYL_COA + NADH + FADH2
- **Equation Type**: First-order linear with stoichiometric products
- **Mathematical Form**: Linear consumption (rate = 0.5) with stoichiometric products based on fatty acid chain length (oleic acid = 18C, produces 8 acetyl-CoA, 8 NADH, 7 FADH2)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/mitochondria/FattyAcidOxidation.kt`

**9. AlanineAminoTransferase**
- **Metabolites Processed**: GLUTAMIC_ACID + PYRUVATE ↔ KETOGLUTARATE + ALANINE
- **Equation Type**: First-order linear (bidirectional, treated as unidirectional in implementation)
- **Mathematical Form**: Conversion rate = 0.3 per minute; coupled stoichiometry (1:1 conversion)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/cells/mitochondria/AlanineAminoTransferase.kt`

---

### **GASTROINTESTINAL TRACT**

**10. Pepsin (Stomach)**
- **Metabolites Processed**: ARACHIN (peanut protein) → POLYPEPTIDE
- **Equation Type**: Exponential first-order with saturation
- **Mathematical Form**: exp(-kt) model with 87% hydrolysis at 120 min; k = 0.08 min⁻¹
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/stomach/PepsinODE.kt`

**11. StomachEmptyingRegulator**
- **Metabolites Processed**: STARCH + MALTOSE + POLYPEPTIDE + TRIOLEIN (all emptied together)
- **Equation Type**: Exponential power-law model
- **Mathematical Form**: d[content]/dt = -LAG × (1-exp(-kt))^(LAG-1) × k × exp(-kt); k=0.0155, LAG=2.62
- **Details**: Sigmoidal emptying pattern characteristic of gastric kinetics
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/stomach/StomachEmptyingRegulator.kt`

**12. SalivaryAmylase (Mouth)**
- **Metabolites Processed**: STARCH → MALTOSE
- **Equation Type**: First-order exponential kinetics
- **Mathematical Form**: d[Maltose]/dt = [Starch] × (1 - exp(-kt)); k=0.03 min⁻¹, t_mouth=0.17 min
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/mouth/SalivaryAmylase.kt`

**13. PancreaticAmylase (Small Intestine)**
- **Metabolites Processed**: STARCH → MALTOSE
- **Equation Type**: First-order exponential kinetics
- **Mathematical Form**: d[Maltose]/dt = [Starch] × (1 - exp(-kt)); k=0.03 min⁻¹ (more time in intestine than mouth)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/PancreaticAmylase.kt`

**14. MembraneMaltoseHydrolysis (Small Intestine)**
- **Metabolites Processed**: MALTOSE → GLUCOSE (2:1 ratio)
- **Equation Type**: Michaelis-Menten kinetics
- **Mathematical Form**: v = k[Maltose]/(Km + [Maltose]), where k=MAX_RATE × JEJUNUM_LENGTH
- **Details**: Brush border enzyme kinetics; Km = Michaelis constant for maltose
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/MaltoseHydrolysisODE.kt`

**15. Lipase (Small Intestine)**
- **Metabolites Processed**: TRIOLEIN → DAG + MAG + OLEIC_ACID
- **Equation Type**: Sequential first-order kinetics with intermediate
- **Mathematical Form**: Coupled exponential decay: Triolein → Diolein → Monolein → Oleic Acid
- **Details**: Two-step process with different rate constants (k_triolein=0.0276, k_diolein=0.243)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/Lipase.kt`

**16. Trypsin (Small Intestine)**
- **Metabolites Processed**: POLYPEPTIDE → PEPTIDE_CHAIN
- **Equation Type**: Zero-order (constant rate)
- **Mathematical Form**: Linear degradation rate = 1.1×10⁻² mmol/min; accounts for MW change in conversion
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/Trypsin.kt`

**17. Peptidase (Small Intestine)**
- **Metabolites Processed**: PEPTIDE_CHAIN → GLUTAMIC_ACID
- **Equation Type**: First-order linear
- **Mathematical Form**: d[Peptide]/dt = -0.2[Peptide]; accounts for MW change from peptide to amino acid
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/Peptidase.kt`

**18. MixedMicelleFormer (Small Intestine)**
- **Metabolites Processed**: MAG + OLEIC_ACID + BILE_SALT → MICELLE
- **Equation Type**: Limit reactant (Michaelis-Menten-like saturation)
- **Mathematical Form**: Rate = 0.5 × min(MAG, OA, BS); consumed in stoichiometry 1:2:1, producing 1 MICELLE
- **Details**: Triple substrate with one rate-limiting (minimum) step
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/MixedMicelleFormer.kt`

**19. Golgi/ChylomicronProducer (Enterocytes)**
- **Metabolites Processed**: MICELLE → CHYLOMICRON
- **Equation Type**: First-order linear
- **Mathematical Form**: Simple first-order kinetics (rate = 0.5)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/intestine/Enterocyte.kt`

---

### **ENDOCRINE ORGANS**

**20. InsulinProduction (Pancreas)**
- **Metabolites Processed**: GLUCOSE + GLUTAMIC_ACID → INSULIN
- **Equation Type**: Linear combination with feedback inhibition
- **Mathematical Form**: d[Insulin]/dt = k_glucose×[Glucose] + k_amino×[AA] - k_insulin×[Insulin] + steady_state_factor
- **Details**: Multiple linear terms with feedback; coefficients: k_g=0.0974, k_aa=0.0247, k_i=0.14
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/pancreas/InsulinProduction.kt`

**21. BileSaltProducer (Liver)**
- **Metabolites Processed**: → BILE_SALT (production/synthesis)
- **Equation Type**: Zero-order (constant production)
- **Mathematical Form**: d[Bile_Salt]/dt = 1.0 (constant production)
- **File**: `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/liver/Liver.kt`

---

## Summary Table

| Metabolizer | Metabolites | Equation Type | Key Feature |
|---|---|---|---|
| GlucoseUptakeRegulator | INSULIN/GLUCOSE | MM + Hill (γ=1.62) | Allosteric regulation |
| InsulinBreakdown | INSULIN | First-order | Exponential decay (t½=4min) |
| LipoproteinLipase | CHYLOMICRON→OLEIC_ACID+GLYCEROL | Zero-order | Mass action kinetics |
| Glycolysis | GLUCOSE→PYRUVATE (7 steps) | Multi-enzyme MM + Hill | Complex regulatory network |
| AcylCoASynthase | OLEIC_ACID→FATTY_ACYL_COA | First-order | Simple linear |
| TCACycle | PYRUVATE+SUBSTRATES→NADH+FADH2+GTP | First-order linear | Stoichiometric products |
| ElectronTransportChain | NADH+FADH2→ATP | MM (saturation) | Capacity-limited |
| FattyAcidOxidation | FATTY_ACYL_COA→ACETYL_COA+NADH+FADH2 | First-order | Chain-length dependent |
| AlanineAminoTransferase | GLUTAMATE+PYRUVATE↔ALANINE+α-KG | First-order | Reversible transamination |
| Pepsin | ARACHIN→POLYPEPTIDE | Exp first-order | 87% max hydrolysis at 120min |
| StomachEmptying | STARCH+others | Exponential power-law | Sigmoidal kinetics (k=0.0155) |
| SalivaryAmylase | STARCH→MALTOSE | Exp first-order | Brief time in mouth (0.17min) |
| PancreaticAmylase | STARCH→MALTOSE | Exp first-order | Longer intestinal residence |
| MembraneMaltoseHydrolysis | MALTOSE→GLUCOSE | Michaelis-Menten | Brush border enzyme |
| Lipase | TRIOLEIN→DAG→MAG→OLEIC_ACID | Sequential 1st-order | Two-step intermediate |
| Trypsin | POLYPEPTIDE→PEPTIDE_CHAIN | Zero-order | Constant linear rate |
| Peptidase | PEPTIDE_CHAIN→GLUTAMIC_ACID | First-order | MW-corrected conversion |
| MixedMicelleFormer | MAG+OA+BS→MICELLE | Limit reactant | Min(substrates) rate-limiting |
| ChylomicronProducer | MICELLE→CHYLOMICRON | First-order | Simple packaging |
| InsulinProduction | GLUCOSE+AA→INSULIN | Linear multi-term | Feedback inhibition |
| BileSaltProducer | →BILE_SALT | Zero-order | Constant synthesis |

---

### File Locations Summary
All metabolizers are located at:
`/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/organ/`

Sub-directories:
- `blood/` - 3 metabolizers
- `cells/` - 2 metabolizers
- `cells/mitochondria/` - 4 metabolizers
- `mouth/` - 1 metabolizer
- `intestine/` - 8 metabolizers
- `stomach/` - 2 metabolizers
- `pancreas/` - 1 metabolizer
- `liver/` - 1 metabolizer

**Total: 21 Metabolizers** with varied kinetic models ranging from simple zero-order and first-order kinetics to complex multi-enzyme Michaelis-Menten systems with allosteric regulation and Hill coefficients.

---

