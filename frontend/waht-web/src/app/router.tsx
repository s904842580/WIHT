import { AppLayout } from '@/layouts/AppLayout';
import { AssetsPage } from '@/pages/AssetsPage';
import { GamesPage } from '@/pages/GamesPage';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { NoteDetailPage } from '@/pages/NoteDetailPage';
import { NotesPage } from '@/pages/NotesPage';
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
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'notes', element: <NotesPage /> },
      { path: 'notes/:slug', element: <NoteDetailPage /> },
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'projects/:slug', element: <ProjectDetailPage /> },
      { path: 'games', element: <GamesPage /> },
      { path: 'assets', element: <AssetsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
