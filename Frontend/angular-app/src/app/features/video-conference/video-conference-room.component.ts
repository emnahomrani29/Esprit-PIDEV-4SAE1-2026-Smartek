import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef,
  ChangeDetectorRef, AfterViewChecked
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

interface Participant {
  sessionId: string;
  userId: string;
  name: string;
  stream?: MediaStream;
  isMuted: boolean;
  isVideoOff: boolean;
  isSpeaking: boolean;
  peerConnection?: RTCPeerConnection;
  videoElementId: string;
}

interface ChatMessage {
  sender: string;
  message: string;
  timestamp: Date;
  isSystem?: boolean;
}

// Consistent avatar colors per name
const AVATAR_COLORS = [
  '#1a73e8', '#0f9d58', '#f4b400', '#db4437',
  '#9c27b0', '#00bcd4', '#ff5722', '#607d8b'
];

@Component({
  selector: 'app-video-conference-room',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './video-conference-room.component.html',
  styleUrls: ['./video-conference-room.component.scss']
})
export class VideoConferenceRoomComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('localVideo') localVideo!: ElementRef<HTMLVideoElement>;
  @ViewChild('screenVideo') screenVideo!: ElementRef<HTMLVideoElement>;
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  roomId = '';
  localStream?: MediaStream;
  screenStream?: MediaStream;
  participants: Participant[] = [];

  // Controls
  isMuted = false;
  isVideoOff = false;
  isSharingScreen = false;
  isSpeaking = false;
  showChat = false;
  showParticipants = false;

  // Screen share
  sharingParticipant: Participant | null = null;

  // Chat
  chatMessages: ChatMessage[] = [];
  newMessage = '';
  unreadMessages = 0;
  private shouldScrollChat = false;

  // User info
  userName = 'Utilisateur';
  userId = '';
  mySessionId = '';

  // Clock
  currentTime = '';
  private clockInterval?: any;

  // WebSocket
  private ws?: WebSocket;
  private wsConnected = false;
  private pendingCandidates = new Map<string, RTCIceCandidateInit[]>();

  private iceServers: RTCIceServer[] = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    this.roomId = this.route.snapshot.paramMap.get('roomId') || '';
    if (!this.roomId) {
      this.router.navigate(['/trainer/courses']);
      return;
    }

    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || userInfo?.email || 'Utilisateur';
    this.userId = String(userInfo?.userId || '');

    this.startClock();
    await this.initializeMedia();
    this.connectSignaling();
  }

  ngAfterViewChecked() {
    if (this.shouldScrollChat && this.chatContainer) {
      const el = this.chatContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScrollChat = false;
    }
  }

  // ─── Clock ────────────────────────────────────────────────────────────────

  startClock() {
    const update = () => {
      const now = new Date();
      this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
      this.cdr.detectChanges();
    };
    update();
    this.clockInterval = setInterval(update, 1000);
  }

  // ─── Grid layout ──────────────────────────────────────────────────────────

  getGridClass(): string {
    const total = this.getParticipantCount();
    if (total === 1) return 'flex items-center justify-center h-full';
    if (total === 2) return 'grid grid-cols-2 gap-3 h-full';
    if (total <= 4) return 'grid grid-cols-2 gap-3 h-full';
    if (total <= 6) return 'grid grid-cols-3 gap-3 h-full';
    return 'grid grid-cols-4 gap-3 h-full';
  }

  // ─── Avatar color ─────────────────────────────────────────────────────────

  getAvatarColor(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
    return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
  }

  // ─── Media ────────────────────────────────────────────────────────────────

  async initializeMedia() {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: { echoCancellation: true, noiseSuppression: true }
      });
      setTimeout(() => {
        if (this.localVideo?.nativeElement && this.localStream) {
          this.localVideo.nativeElement.srcObject = this.localStream;
        }
      }, 100);
    } catch {
      this.addSystemMessage('⚠️ Impossible d\'accéder à la caméra/micro.');
    }
  }

  // ─── WebSocket Signaling ──────────────────────────────────────────────────

  connectSignaling() {
    // WebSocket via l'API Gateway (port 8090) — ne pas se connecter directement au course-service
    const wsUrl = 'ws://localhost:8090/ws/signaling';
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.wsConnected = true;
      this.sendSignal({ type: 'join', roomId: this.roomId, userName: this.userName, userId: this.userId });
    };

    this.ws.onmessage = (event) => {
      try { this.handleSignalingMessage(JSON.parse(event.data)); } catch {}
    };

    this.ws.onclose = (e) => {
      this.wsConnected = false;
      if (e.code !== 1000) setTimeout(() => { if (this.roomId) this.connectSignaling(); }, 3000);
    };

    this.ws.onerror = () => {
      this.addSystemMessage('⚠️ Connexion au serveur de signalisation échouée.');
    };
  }

  sendSignal(data: any) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    }
  }

  async handleSignalingMessage(msg: any) {
    switch (msg.type) {
      case 'session-id':
        this.mySessionId = msg.sessionId;
        this.addSystemMessage('✅ Connecté à la salle');
        break;

      case 'existing-participant':
        await this.createPeerConnection(msg.sessionId, msg.userName, msg.userId, true);
        break;

      case 'user-joined':
        if (msg.sessionId !== this.mySessionId) {
          this.addSystemMessage(`👤 ${msg.userName} a rejoint`);
          await this.createPeerConnection(msg.sessionId, msg.userName, msg.userId, false);
        }
        break;

      case 'offer':
        await this.handleOffer(msg.from, msg.sdp);
        break;

      case 'answer':
        await this.handleAnswer(msg.from, msg.sdp);
        break;

      case 'ice-candidate':
        await this.handleIceCandidate(msg.from, msg.candidate);
        break;

      case 'user-left':
        if (msg.sessionId !== this.mySessionId) {
          this.removeParticipant(msg.sessionId);
          this.addSystemMessage(`👋 ${msg.userName} a quitté`);
        }
        break;

      case 'chat':
        if (msg.from !== this.mySessionId) {
          this.chatMessages.push({ sender: msg.userName, message: msg.message, timestamp: new Date(msg.timestamp) });
          if (!this.showChat) this.unreadMessages++;
          this.shouldScrollChat = true;
          this.cdr.detectChanges();
        }
        break;

      case 'media-state':
        const p = this.participants.find(x => x.sessionId === msg.from);
        if (p) {
          p.isMuted = msg.isMuted;
          p.isVideoOff = msg.isVideoOff;
          this.cdr.detectChanges();
        }
        break;

      case 'screen-share':
        const sp = this.participants.find(x => x.sessionId === msg.from);
        if (sp) {
          this.sharingParticipant = msg.sharing ? sp : null;
          this.cdr.detectChanges();
        }
        break;
    }
  }

  // ─── WebRTC ───────────────────────────────────────────────────────────────

  async createPeerConnection(sessionId: string, name: string, userId: string, isInitiator: boolean): Promise<RTCPeerConnection> {
    const pc = new RTCPeerConnection({ iceServers: this.iceServers });

    const participant: Participant = {
      sessionId, userId, name,
      isMuted: false, isVideoOff: false, isSpeaking: false,
      peerConnection: pc,
      videoElementId: `video-${sessionId}`
    };
    this.participants.push(participant);
    this.cdr.detectChanges();

    // Add local tracks
    if (this.localStream) {
      this.localStream.getTracks().forEach(t => pc.addTrack(t, this.localStream!));
    }

    // Remote stream
    pc.ontrack = (event) => {
      participant.stream = event.streams[0];
      this.cdr.detectChanges();
      setTimeout(() => {
        const el = document.getElementById(participant.videoElementId) as HTMLVideoElement;
        if (el) el.srcObject = participant.stream!;
      }, 100);
    };

    // ICE
    pc.onicecandidate = (e) => {
      if (e.candidate) {
        this.sendSignal({ type: 'ice-candidate', roomId: this.roomId, to: sessionId, candidate: e.candidate.toJSON() });
      }
    };

    pc.onconnectionstatechange = () => {
      if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
        this.removeParticipant(sessionId);
      }
    };

    // Apply pending candidates
    for (const c of this.pendingCandidates.get(sessionId) || []) {
      await pc.addIceCandidate(new RTCIceCandidate(c));
    }
    this.pendingCandidates.delete(sessionId);

    if (isInitiator) {
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);
      this.sendSignal({ type: 'offer', roomId: this.roomId, to: sessionId, sdp: pc.localDescription });
    }

    return pc;
  }

  async handleOffer(fromSession: string, sdp: RTCSessionDescriptionInit) {
    let p = this.participants.find(x => x.sessionId === fromSession);
    let pc: RTCPeerConnection;
    if (!p) {
      pc = await this.createPeerConnection(fromSession, 'Participant', '', false);
      p = this.participants.find(x => x.sessionId === fromSession)!;
    } else {
      pc = p.peerConnection!;
    }
    await pc.setRemoteDescription(new RTCSessionDescription(sdp));
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    this.sendSignal({ type: 'answer', roomId: this.roomId, to: fromSession, sdp: pc.localDescription });
  }

  async handleAnswer(fromSession: string, sdp: RTCSessionDescriptionInit) {
    const p = this.participants.find(x => x.sessionId === fromSession);
    if (p?.peerConnection) await p.peerConnection.setRemoteDescription(new RTCSessionDescription(sdp));
  }

  async handleIceCandidate(fromSession: string, candidate: RTCIceCandidateInit) {
    const p = this.participants.find(x => x.sessionId === fromSession);
    if (p?.peerConnection?.remoteDescription) {
      await p.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    } else {
      if (!this.pendingCandidates.has(fromSession)) this.pendingCandidates.set(fromSession, []);
      this.pendingCandidates.get(fromSession)!.push(candidate);
    }
  }

  removeParticipant(sessionId: string) {
    const idx = this.participants.findIndex(p => p.sessionId === sessionId);
    if (idx !== -1) {
      const p = this.participants[idx];
      p.peerConnection?.close();
      p.stream?.getTracks().forEach(t => t.stop());
      this.participants.splice(idx, 1);
      if (this.sharingParticipant?.sessionId === sessionId) this.sharingParticipant = null;
      this.cdr.detectChanges();
    }
  }

  // ─── Controls ─────────────────────────────────────────────────────────────

  toggleMute() {
    const track = this.localStream?.getAudioTracks()[0];
    if (track) {
      track.enabled = !track.enabled;
      this.isMuted = !track.enabled;
      this.sendSignal({ type: 'media-state', roomId: this.roomId, isMuted: this.isMuted, isVideoOff: this.isVideoOff });
    }
  }

  toggleVideo() {
    const track = this.localStream?.getVideoTracks()[0];
    if (track) {
      track.enabled = !track.enabled;
      this.isVideoOff = !track.enabled;
      this.sendSignal({ type: 'media-state', roomId: this.roomId, isMuted: this.isMuted, isVideoOff: this.isVideoOff });
    }
  }

  async toggleScreenShare() {
    this.isSharingScreen ? await this.stopScreenShare() : await this.startScreenShare();
  }

  async startScreenShare() {
    try {
      this.screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: false });
      const screenTrack = this.screenStream.getVideoTracks()[0];

      // Show in the main screen video element
      setTimeout(() => {
        if (this.screenVideo?.nativeElement) {
          this.screenVideo.nativeElement.srcObject = this.screenStream!;
        }
      }, 100);

      // Replace video track in all peer connections
      this.participants.forEach(p => {
        const sender = p.peerConnection?.getSenders().find(s => s.track?.kind === 'video');
        if (sender) sender.replaceTrack(screenTrack);
      });

      this.isSharingScreen = true;
      this.sendSignal({ type: 'screen-share', roomId: this.roomId, sharing: true });
      this.addSystemMessage('🖥️ Partage d\'écran démarré');

      screenTrack.onended = () => this.stopScreenShare();
    } catch (e) {
      console.error('Screen share error:', e);
    }
  }

  async stopScreenShare() {
    this.screenStream?.getTracks().forEach(t => t.stop());
    this.screenStream = undefined;

    // Restore camera track
    const cameraTrack = this.localStream?.getVideoTracks()[0];
    if (cameraTrack) {
      this.participants.forEach(p => {
        const sender = p.peerConnection?.getSenders().find(s => s.track?.kind === 'video');
        if (sender) sender.replaceTrack(cameraTrack);
      });
    }

    this.isSharingScreen = false;
    this.sendSignal({ type: 'screen-share', roomId: this.roomId, sharing: false });
    this.addSystemMessage('🖥️ Partage d\'écran arrêté');
    this.cdr.detectChanges();
  }

  // ─── Chat ─────────────────────────────────────────────────────────────────

  toggleChat() {
    this.showChat = !this.showChat;
    if (this.showChat) { this.showParticipants = false; this.unreadMessages = 0; this.shouldScrollChat = true; }
  }

  toggleParticipants() {
    this.showParticipants = !this.showParticipants;
    if (this.showParticipants) this.showChat = false;
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;
    this.chatMessages.push({ sender: this.userName, message: this.newMessage, timestamp: new Date() });
    this.sendSignal({ type: 'chat', roomId: this.roomId, message: this.newMessage });
    this.newMessage = '';
    this.shouldScrollChat = true;
  }

  addSystemMessage(message: string) {
    this.chatMessages.push({ sender: 'Système', message, timestamp: new Date(), isSystem: true });
    this.shouldScrollChat = true;
    this.cdr.detectChanges();
  }

  // ─── Leave ────────────────────────────────────────────────────────────────

  leaveRoom() {
    if (confirm('Quitter la session ?')) {
      this.cleanup();
      const userInfo = this.authService.getUserInfo();
      const role = userInfo?.role?.toUpperCase();
      if (role === 'TRAINER' || role === 'ROLE_TRAINER') {
        this.router.navigate(['/trainer/courses']);
      } else {
        this.router.navigate(['/learner-courses']);
      }
    }
  }

  cleanup() {
    this.sendSignal({ type: 'leave', roomId: this.roomId });
    this.participants.forEach(p => { p.peerConnection?.close(); p.stream?.getTracks().forEach(t => t.stop()); });
    this.participants = [];
    this.localStream?.getTracks().forEach(t => t.stop());
    this.screenStream?.getTracks().forEach(t => t.stop());
    if (this.ws) { this.ws.close(1000, 'User left'); this.ws = undefined; }
    if (this.clockInterval) clearInterval(this.clockInterval);
  }

  ngOnDestroy() { this.cleanup(); }

  getParticipantCount(): number { return this.participants.length + 1; }
}
