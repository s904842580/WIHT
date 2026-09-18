import { AppLayout } from '@/layouts/AppLayout';
import { AuditLogsPage } from '@/pages/AuditLogsPage';
import { ProtectedRoute } from '@/features/auth/ProtectedRoute';
import { AssetsPage } from '@/pages/AssetsPage';
import { AgentWorkspacePage } from '@/pages/AgentWorkspacePage';
import { GamesPage } from '@/pages/GamesPage';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { NoteDetailPage } from '@/pages/NoteDetailPage';
import { NoteEditorPage } from '@/pages/NoteEditorPage';
import { NotesPage } from '@/pages/NotesPage';
import { MyNotesPage } from '@/pages/MyNotesPage';
import { ProjectDetailPage } from '@/pages/ProjectDetailPage';
import { ProjectsPage } from '@/pages/ProjectsPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { createBrowserRouter } from 'react-router-dom';

// 路由表：URL 和页面组件的对应关系。
export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { path: 'workspace/audit', element: <ProtectedRoute><AuditLogsPage /></ProtectedRoute> },
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'notes', element: <NotesPage /> },
      { path: 'notes/:slug', element: <NoteDetailPage /> },
      {
        path: 'workspace/notes',
        element: (
          <ProtectedRoute>
            <MyNotesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'workspace/agent',
        element: (
          <ProtectedRoute>
            <AgentWorkspacePage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'workspace/notes/new',
        element: (
          <ProtectedRoute>
            <NoteEditorPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'workspace/notes/:noteId/edit',
        element: (
          <ProtectedRoute>
            <NoteEditorPage />
          </ProtectedRoute>
        ),
      },
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'projects/:slug', element: <ProjectDetailPage /> },
      { path: 'games', element: <GamesPage /> },
      { path: 'assets', element: <AssetsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
