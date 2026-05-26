export interface FieldPlot {
  id?: number
  plotCode: string
  plotName: string
  region: string
  county?: string
  town?: string
  longitude: number
  latitude: number
  areaMu: number
  soilType?: string
  phValue: number
  organicMatter: number
  seleniumContent: number
  salinity: number
  electricalConductivity: number
  heavyMetalRisk: string
  riskLevel: string
  irrigationType?: string
  managerName?: string
  status?: string
  remark?: string
}

export interface CropVariety {
  id?: number
  cropType: string
  varietyName: string
  seleniumPotential?: number
  growthPeriodDays?: number
}

export interface CropRecord {
  id?: number
  plotId: number
  varietyId: number
  cropType: string
  varietyName: string
  seasonYear: number
  sowingDate?: string
  harvestDate?: string
  expectedHarvestDate?: string
  plantingAreaMu?: number
  irrigationMethod?: string
  fertilizerMethod?: string
  irrigationMode?: string
  fertilizerPlan?: string
  yieldKgMu?: number
  qualityLevel?: string
  growthStatus?: string
  remark?: string
  plotName?: string
  region?: string
  plotSeleniumContent?: number
  plotPhValue?: number
  plotRiskLevel?: string
}

export interface SoilSample {
  id?: number
  plotId: number
  sampleCode: string
  sampleDate: string
  phValue: number
  organicMatter?: number
  availableNitrogen?: number
  availablePhosphorus?: number
  availablePotassium?: number
  seleniumContent?: number
  salinity?: number
  electricalConductivity?: number
  heavyMetalRisk?: string
  riskLevel?: string
  soilMoisture?: number
  sampler?: string
  remark?: string
}

export interface WeatherRecord {
  id?: number
  region: string
  recordDate: string
  avgTemp?: number
  maxTemp?: number
  minTemp?: number
  precipitation?: number
  sunshineHours?: number
  solarRadiation?: number
  windSpeed?: number
  humidity?: number
}

export interface SeleniumPrediction {
  id?: number
  plotId: number
  cropRecordId?: number
  soilSampleId?: number
  cropType: string
  varietyName: string
  soilSelenium: number
  phValue: number
  organicMatter: number
  salinity: number
  electricalConductivity: number
  avgTemperature: number
  precipitation: number
  irrigationMethod: string
  fertilizerMethod: string
  predictedSelenium: number
  qualityLevel: string
  confidence: number
  riskLevel: string
  modelVersion: string
  factorContribution?: string
  factorExplanation?: string
  suggestion?: string
  inputSignature?: string
  createdBy?: string
  createdAt?: string
}

export interface SoilAssessment {
  id?: number
  plotId: number
  soilSampleId: number
  assessmentScore: number
  riskLevel: string
  phLevel: string
  fertilityLevel: string
  salinityLevel: string
  seleniumLevel: string
  conductivityLevel?: string
  heavyMetalLevel?: string
  itemEvaluation?: string
  radarJson?: string
  constraintFactor: string
  overallComment?: string
  improvementAdvice: string
  createdAt?: string
}
