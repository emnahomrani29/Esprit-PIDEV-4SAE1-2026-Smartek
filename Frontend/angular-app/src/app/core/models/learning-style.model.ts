export type LearningStyleType = 'VISUAL' | 'AUDITORY' | 'READ_WRITE' | 'KINESTHETIC' | 'MULTIMODAL';

export interface LearningStylePreferenceRequest {
  preferredStyle: LearningStyleType;
  videoPreferred: boolean;
  textPreferred: boolean;
  practicalWorkPreferred: boolean;
  learnerId: number;
  learnerName?: string;
}

export interface LearningStylePreferenceResponse {
  id: number;
  preferredStyle: LearningStyleType;
  videoPreferred: boolean;
  textPreferred: boolean;
  practicalWorkPreferred: boolean;
  lastUpdated: string;
  learnerId: number;
  learnerName?: string;
}
