from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import math

app = FastAPI(title="Recommendation Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ─── DTOs ────────────────────────────────────────────────────────────────────

class LearnerProfile(BaseModel):
    learnerId: int
    completedPaths: List[str] = []
    skillCategories: List[str] = []
    preferredStyle: Optional[str] = None
    averageScore: Optional[float] = None
    progress: Optional[int] = None

class AvailablePath(BaseModel):
    pathId: int
    title: str
    description: Optional[str] = None
    tags: List[str] = []

class RecommendationRequest(BaseModel):
    learner: LearnerProfile
    availablePaths: List[AvailablePath]
    topN: int = 5

class RecommendationItem(BaseModel):
    pathId: int
    title: str
    score: float
    reason: str

class RecommendationResponse(BaseModel):
    learnerId: int
    recommendations: List[RecommendationItem]


# ─── Similarité cosinus pure Python ──────────────────────────────────────────

def cosine_similarity(vec_a: List[float], vec_b: List[float]) -> float:
    dot = sum(a * b for a, b in zip(vec_a, vec_b))
    norm_a = math.sqrt(sum(a * a for a in vec_a))
    norm_b = math.sqrt(sum(b * b for b in vec_b))
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)

def build_vector(tags: List[str], all_tags: List[str]) -> List[float]:
    return [1.0 if t in tags else 0.0 for t in all_tags]

def get_reason(learner: LearnerProfile, path: AvailablePath) -> str:
    path_tags = set(path.tags)
    learner_tags = set(learner.completedPaths + learner.skillCategories)
    common = path_tags & learner_tags
    if common:
        return f"Basé sur vos compétences : {', '.join(list(common)[:3])}"
    if learner.preferredStyle and learner.preferredStyle in path_tags:
        return f"Correspond à votre style d'apprentissage {learner.preferredStyle}"
    return "Recommandé selon votre profil d'apprentissage"


# ─── Endpoints ───────────────────────────────────────────────────────────────

@app.get("/health")
def health():
    return {"status": "UP", "service": "recommendation-service"}


@app.get("/api/recommendations/learner/{learner_id}", response_model=RecommendationResponse)
def get_recommendations_by_learner(learner_id: int, topN: int = 5):
    """
    Endpoint GET appelé directement par le frontend Angular.
    Retourne des recommandations génériques basées sur l'ID learner.
    En production, le learning-service Java enrichit le profil avant d'appeler le POST.
    """
    # Parcours de démonstration — en prod ces données viennent du learning-service Java
    sample_paths = [
        AvailablePath(pathId=1, title="Maîtriser Spring Boot", tags=["Java", "Spring", "Backend", "microservices"]),
        AvailablePath(pathId=2, title="Angular Avancé", tags=["Frontend", "Angular", "TypeScript", "VISUAL"]),
        AvailablePath(pathId=3, title="Docker & Kubernetes", tags=["DevOps", "Docker", "Cloud"]),
        AvailablePath(pathId=4, title="Machine Learning Python", tags=["Python", "ML", "Data", "IA"]),
        AvailablePath(pathId=5, title="Architecture Microservices", tags=["Java", "microservices", "Backend", "Cloud"]),
        AvailablePath(pathId=6, title="React & Redux", tags=["Frontend", "JavaScript", "React", "VISUAL"]),
        AvailablePath(pathId=7, title="CI/CD avec Jenkins", tags=["DevOps", "Jenkins", "automation"]),
    ]

    # Profil générique basé sur l'ID (en prod, enrichi par le Java)
    learner = LearnerProfile(
        learnerId=learner_id,
        completedPaths=[],
        skillCategories=["Backend", "Java"],
        preferredStyle="VISUAL"
    )

    request = RecommendationRequest(learner=learner, availablePaths=sample_paths, topN=topN)
    return get_recommendations(request)


@app.post("/api/recommendations", response_model=RecommendationResponse)
def get_recommendations(request: RecommendationRequest):
    learner = request.learner
    available = request.availablePaths
    top_n = min(request.topN, len(available))

    if not available:
        return RecommendationResponse(learnerId=learner.learnerId, recommendations=[])

    # Tous les tags possibles (union learner + parcours)
    all_tags = list({
        tag
        for path in available
        for tag in path.tags
    } | set(learner.completedPaths) | set(learner.skillCategories))

    if learner.preferredStyle:
        all_tags.append(learner.preferredStyle)
    all_tags = list(set(all_tags))  # dédupliquer

    # Vecteur learner
    learner_tags = learner.completedPaths + learner.skillCategories
    if learner.preferredStyle:
        learner_tags = learner_tags + [learner.preferredStyle]
    learner_vec = build_vector(learner_tags, all_tags)

    # Scorer chaque parcours
    scored = []
    for path in available:
        # Ignorer les parcours déjà complétés
        if path.title in learner.completedPaths:
            continue

        path_vec = build_vector(path.tags, all_tags)
        score = cosine_similarity(learner_vec, path_vec)

        # Boost si style préféré correspond
        if learner.preferredStyle and learner.preferredStyle in path.tags:
            score = min(1.0, score + 0.1)

        scored.append((path, score))

    # Trier par score décroissant
    scored.sort(key=lambda x: x[1], reverse=True)

    recommendations = [
        RecommendationItem(
            pathId=path.pathId,
            title=path.title,
            score=round(score, 4),
            reason=get_reason(learner, path)
        )
        for path, score in scored[:top_n]
    ]

    return RecommendationResponse(learnerId=learner.learnerId, recommendations=recommendations)
