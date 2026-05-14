import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LearningStyleService } from '../../../core/services/learning-style.service';
import { AuthService } from '../../../core/services/auth.service';
import { LearningStyleType } from '../../../core/models/learning-style.model';

interface QuizQuestion {
  id: number;
  question: string;
  options: { text: string; style: LearningStyleType }[];
}

@Component({
  selector: 'app-learner-learning-style-quiz',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './learner-learning-style-quiz.component.html',
  styleUrl: './learner-learning-style-quiz.component.scss'
})
export class LearnerLearningStyleQuizComponent {
  currentStep: 'intro' | 'quiz' | 'result' = 'intro';
  currentQuestionIndex = 0;
  answers: LearningStyleType[] = [];
  result: LearningStyleType | null = null;
  isSaving = false;
  saveSuccess = false;
  saveError = '';

  readonly introStyles: LearningStyleType[] = ['VISUAL', 'AUDITORY', 'READ_WRITE', 'KINESTHETIC'];

  readonly questions: QuizQuestion[] = [
    {
      id: 1,
      question: 'When you need to learn something new, what do you prefer?',
      options: [
        { text: '📊 Watch a video or look at diagrams and charts', style: 'VISUAL' },
        { text: '🎧 Listen to an explanation or podcast', style: 'AUDITORY' },
        { text: '📖 Read a detailed article or documentation', style: 'READ_WRITE' },
        { text: '🛠️ Try it out directly with a hands-on exercise', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 2,
      question: 'When you remember something you learned, how do you recall it?',
      options: [
        { text: '🖼️ I see images, colors or diagrams in my mind', style: 'VISUAL' },
        { text: '🔊 I hear the voice or sounds associated with it', style: 'AUDITORY' },
        { text: '📝 I remember words, lists or written notes', style: 'READ_WRITE' },
        { text: '💪 I remember the feeling of doing it', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 3,
      question: 'When you have a problem to solve, you prefer to:',
      options: [
        { text: '📐 Draw a diagram or mind map to visualize it', style: 'VISUAL' },
        { text: '🗣️ Talk it through with someone or think out loud', style: 'AUDITORY' },
        { text: '✍️ Write down the problem and possible solutions', style: 'READ_WRITE' },
        { text: '🔧 Experiment and try different approaches', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 4,
      question: 'In a classroom or training session, you learn best when:',
      options: [
        { text: '📽️ The instructor uses slides, videos and visual aids', style: 'VISUAL' },
        { text: '🎤 There are discussions, debates and verbal explanations', style: 'AUDITORY' },
        { text: '📚 There are handouts, readings and written exercises', style: 'READ_WRITE' },
        { text: '🏗️ There are practical activities and real projects', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 5,
      question: 'When you take notes, you tend to:',
      options: [
        { text: '🎨 Use colors, symbols and draw sketches', style: 'VISUAL' },
        { text: '🎙️ Record audio or prefer not to take notes', style: 'AUDITORY' },
        { text: '📋 Write detailed and organized notes', style: 'READ_WRITE' },
        { text: '✏️ Write very little and prefer to do things', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 6,
      question: 'When you follow instructions for a new task, you prefer:',
      options: [
        { text: '🗺️ A visual guide with pictures and diagrams', style: 'VISUAL' },
        { text: '📢 Someone explains it to you verbally', style: 'AUDITORY' },
        { text: '📄 Written step-by-step instructions', style: 'READ_WRITE' },
        { text: '▶️ Just start doing it and learn as you go', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 7,
      question: 'When you are bored in a meeting or class, you tend to:',
      options: [
        { text: '✏️ Doodle or draw patterns on paper', style: 'VISUAL' },
        { text: '🎵 Hum or tap rhythms quietly', style: 'AUDITORY' },
        { text: '📝 Write notes or make lists', style: 'READ_WRITE' },
        { text: '🚶 Move around, fidget or want to get up', style: 'KINESTHETIC' }
      ]
    },
    {
      id: 8,
      question: 'To prepare for an important exam, you would:',
      options: [
        { text: '📊 Create charts, diagrams and visual summaries', style: 'VISUAL' },
        { text: '🔁 Repeat information out loud or record yourself', style: 'AUDITORY' },
        { text: '📖 Re-read notes and write summaries', style: 'READ_WRITE' },
        { text: '🎯 Practice with real exercises and simulations', style: 'KINESTHETIC' }
      ]
    }
  ];

  readonly styleInfo: Record<LearningStyleType, {
    label: string; icon: string; emoji: string;
    description: string; color: string; bgColor: string;
    strengths: string[]; tips: string[];
  }> = {
    VISUAL: {
      label: 'Visual Learner', icon: 'visibility', emoji: '👁️',
      description: 'You learn best through images, diagrams, charts and visual representations.',
      color: '#3b82f6', bgColor: '#eff6ff',
      strengths: ['Excellent at reading maps and charts', 'Strong spatial awareness', 'Good at remembering faces and places'],
      tips: ['Use mind maps and diagrams', 'Watch video tutorials', 'Use color-coding in your notes', 'Create visual summaries']
    },
    AUDITORY: {
      label: 'Auditory Learner', icon: 'hearing', emoji: '🎧',
      description: 'You learn best through listening, discussions and verbal explanations.',
      color: '#8b5cf6', bgColor: '#f5f3ff',
      strengths: ['Good at following verbal instructions', 'Strong listening skills', 'Learns well from discussions'],
      tips: ['Listen to podcasts and audiobooks', 'Explain concepts out loud', 'Join study groups', 'Record and replay your notes']
    },
    READ_WRITE: {
      label: 'Read/Write Learner', icon: 'menu_book', emoji: '📖',
      description: 'You learn best through reading and writing. You prefer text-based information.',
      color: '#10b981', bgColor: '#ecfdf5',
      strengths: ['Strong reading comprehension', 'Good at writing and summarizing', 'Organized and detail-oriented'],
      tips: ['Take detailed notes', 'Read articles and documentation', 'Write summaries after each session', 'Make lists and outlines']
    },
    KINESTHETIC: {
      label: 'Kinesthetic Learner', icon: 'sports_handball', emoji: '🛠️',
      description: 'You learn best through hands-on experience, practice and movement.',
      color: '#f59e0b', bgColor: '#fffbeb',
      strengths: ['Excellent at practical tasks', 'Good at problem-solving by doing', 'Strong muscle memory'],
      tips: ['Practice with real projects', 'Take frequent breaks', 'Use simulations and labs', 'Learn by teaching others']
    },
    MULTIMODAL: {
      label: 'Multimodal Learner', icon: 'auto_awesome', emoji: '✨',
      description: 'You adapt to multiple learning styles depending on the context.',
      color: '#ec4899', bgColor: '#fdf2f8',
      strengths: ['Highly adaptable', 'Can learn in any environment', 'Versatile and flexible'],
      tips: ['Combine multiple methods', 'Adapt your approach to the subject', 'Use a mix of videos, texts and practice', 'Be flexible in your learning strategy']
    }
  };

  private currentUser: any = null;

  constructor(
    private learningStyleService: LearningStyleService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUser = this.authService.getUserInfo();
  }

  startQuiz(): void {
    this.currentStep = 'quiz';
    this.currentQuestionIndex = 0;
    this.answers = [];
  }

  selectAnswer(style: LearningStyleType): void {
    this.answers[this.currentQuestionIndex] = style;
    if (this.currentQuestionIndex < this.questions.length - 1) {
      setTimeout(() => { this.currentQuestionIndex++; }, 300);
    } else {
      setTimeout(() => { this.calculateResult(); }, 300);
    }
  }

  calculateResult(): void {
    const counts: Record<string, number> = {};
    this.answers.forEach(a => { counts[a] = (counts[a] || 0) + 1; });
    const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1]);
    const topCount = sorted[0][1];
    const topStyles = sorted.filter(s => s[1] === topCount);
    this.result = topStyles.length > 1 ? 'MULTIMODAL' : sorted[0][0] as LearningStyleType;
    this.currentStep = 'result';
  }

  saveResult(): void {
    if (!this.result || !this.currentUser?.userId) return;
    this.isSaving = true;
    this.saveError = '';
    this.learningStyleService.save({
      preferredStyle: this.result,
      videoPreferred: this.result === 'VISUAL' || this.result === 'MULTIMODAL',
      textPreferred: this.result === 'READ_WRITE' || this.result === 'MULTIMODAL',
      practicalWorkPreferred: this.result === 'KINESTHETIC' || this.result === 'MULTIMODAL',
      learnerId: Number(this.currentUser.userId),
      learnerName: this.currentUser.firstName || ''
    }).subscribe({
      next: () => {
        this.isSaving = false;
        this.saveSuccess = true;
        setTimeout(() => { this.router.navigate(['/learner/learning-style']); }, 2000);
      },
      error: (err) => {
        this.saveError = err.error?.message || 'Error saving your result';
        this.isSaving = false;
      }
    });
  }

  retakeQuiz(): void {
    this.currentStep = 'intro';
    this.answers = [];
    this.result = null;
    this.saveSuccess = false;
    this.saveError = '';
  }

  getProgress(): number {
    return Math.round(((this.currentQuestionIndex + 1) / this.questions.length) * 100);
  }

  getStyleInfo(style: string) {
    return this.styleInfo[style as LearningStyleType];
  }

  getScoreBreakdown(): { style: LearningStyleType; count: number; percentage: number }[] {
    const counts: Record<string, number> = {};
    this.answers.forEach(a => { counts[a] = (counts[a] || 0) + 1; });
    return (['VISUAL', 'AUDITORY', 'READ_WRITE', 'KINESTHETIC'] as LearningStyleType[])
      .map(s => ({
        style: s,
        count: counts[s] || 0,
        percentage: Math.round(((counts[s] || 0) / this.questions.length) * 100)
      }))
      .sort((a, b) => b.count - a.count);
  }
}
