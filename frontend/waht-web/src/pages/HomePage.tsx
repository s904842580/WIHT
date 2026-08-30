import archiveTriptych from '@/assets/archive-triptych.webp';
import odetteCurrent from '@/assets/odette-current.avif';
import { notesApi } from '@/api/notes';
import { projectsApi } from '@/api/projects';
import { useAuth } from '@/features/auth/AuthProvider';
import type { NoteSummary, ProjectSummary } from '@/types/api';
import { formatDate } from '@/utils/date';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, BookOpen, LogIn, PenLine, UserRound } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { CSSProperties } from 'react';
import { Link } from 'react-router-dom';

type HeroArtwork = 'odette' | 'technology' | 'notes';

type HeroSlide = {
  id: string;
  eyebrow: string;
  title: string;
  subtitle: string;
  linkLabel: string;
  to: string;
  artwork: HeroArtwork;
  artworkLabel: string;
};

type ArchiveEntry = {
  id: string;
  title: string;
  summary: string;
  to: string;
  actionLabel: string;
  techLabels: string[];
  project?: ProjectSummary;
};

const HERO_SLIDES: readonly HeroSlide[] = [
  {
    id: 'odette',
    eyebrow: '现在正在欣赏',
    title: '《原神》',
    subtitle: '奥黛塔 · 柔雪的幻象',
    linkLabel: '查看我的记录',
    to: '/notes?keyword=原神',
    artwork: 'odette',
    artworkLabel: '《原神》奥黛塔角色画面',
  },
  {
    id: 'notes',
    eyebrow: '最近正在整理',
    title: '学习手记',
    subtitle: '把零散结论整理成能够复用的知识',
    linkLabel: '翻阅公开手记',
    to: '/notes',
    artwork: 'notes',
    artworkLabel: '学习手记与结构草图',
  },
  {
    id: 'technology',
    eyebrow: '持续正在构建',
    title: '个人项目空间',
    subtitle: '从后端闭环走向可运行、可讲述的作品',
    linkLabel: '进入项目档案',
    to: '/projects',
    artwork: 'technology',
    artworkLabel: '个人技术实验画面',
  },
];

const PLANNED_ARCHIVES: readonly ArchiveEntry[] = [
  {
    id: 'planned-game',
    title: '下一件游戏创作',
    summary: '代表项目尚未确定，先保留一个能够容纳玩法原型和开发记录的位置。',
    to: '/projects',
    actionLabel: '查看项目规划',
    techLabels: ['待定义'],
  },
  {
    id: 'planned-ai',
    title: '下一件 AI 实验',
    summary: '为未来的 Agent、Memory 与角色交互实验预留作品入口。',
    to: '/projects',
    actionLabel: '查看项目规划',
    techLabels: ['待定义'],
  },
  {
    id: 'planned-visual',
    title: '下一件视觉作品',
    summary: '保存游戏场景、角色表达与个人视觉探索的成长过程。',
    to: '/projects',
    actionLabel: '查看项目规划',
    techLabels: ['待定义'],
  },
];

function getArchivePosition(index: number): string {
  if (index % 3 === 0) {
    return '0% center';
  }
  if (index % 3 === 1) {
    return '50% center';
  }
  return '100% center';
}

function getHeroArtworkStyle(artwork: HeroArtwork): CSSProperties {
  if (artwork === 'odette') {
    return {
      backgroundImage: `url("${odetteCurrent}")`,
      backgroundPosition: 'center 46%',
      backgroundSize: 'cover',
    };
  }

  return {
    backgroundImage: `url("${archiveTriptych}")`,
    backgroundPosition: artwork === 'technology' ? '50% center' : '100% center',
    backgroundSize: '300% auto',
  };
}

function formatCompactDate(note: NoteSummary): string {
  const formattedDate = formatDate(note.publishedAt);
  if (formattedDate === '未发布') {
    return '--.--';
  }

  const dateParts = formattedDate.split('/');
  return dateParts.length === 3 ? `${dateParts[1]}.${dateParts[2]}` : formattedDate;
}

function buildArchiveEntries(projects: ProjectSummary[]): ArchiveEntry[] {
  const projectEntries = projects.slice(0, 3).map<ArchiveEntry>((project) => ({
    id: project.slug,
    title: project.name,
    summary: project.summary ?? '这个项目的说明还在整理中。',
    to: `/projects?focus=${encodeURIComponent(project.slug)}`,
    actionLabel: '进入项目页',
    techLabels: project.techStacks.slice(0, 3).map((techStack) => techStack.name),
    project,
  }));

  return [...projectEntries, ...PLANNED_ARCHIVES].slice(0, 3);
}

// HomePage 是个人展示首页：首屏表达当前状态，下方连接真实笔记和项目数据。
export function HomePage() {
  const { user } = useAuth();
  const [activeSlideIndex, setActiveSlideIndex] = useState<number>(0);
  const [isCarouselPaused, setIsCarouselPaused] = useState<boolean>(false);

  const projectsQuery = useQuery({
    queryKey: ['projects', 'home'],
    queryFn: projectsApi.listProjects,
  });
  const notesQuery = useQuery({
    queryKey: ['notes', 'home'],
    queryFn: () => notesApi.listNotes({ page: 1, pageSize: 5 }),
  });

  useEffect(() => {
    if (isCarouselPaused) {
      return undefined;
    }

    const timerId = window.setInterval(() => {
      setActiveSlideIndex((currentIndex) => (currentIndex + 1) % HERO_SLIDES.length);
    }, 7000);

    return () => window.clearInterval(timerId);
  }, [isCarouselPaused]);

  const activeSlide = HERO_SLIDES[activeSlideIndex];
  const archiveEntries = buildArchiveEntries(projectsQuery.data ?? []);
  const recentNotes = notesQuery.data?.items ?? [];

  return (
    <div className="waht-home">
      <section
        className="home-hero"
        onMouseEnter={() => setIsCarouselPaused(true)}
        onMouseLeave={() => setIsCarouselPaused(false)}
        onFocusCapture={() => setIsCarouselPaused(true)}
        onBlurCapture={() => setIsCarouselPaused(false)}
      >
        <div
          key={activeSlide.id}
          role="img"
          aria-label={activeSlide.artworkLabel}
          className="home-hero-artwork"
          style={getHeroArtworkStyle(activeSlide.artwork)}
        />
        <div className="home-hero-overlay" />

        <header className="home-header">
          <Link to="/" className="focus-ring home-wordmark" aria-label="WAHT 首页">
            WAHT
          </Link>

          <nav className="home-nav" aria-label="首页导航">
            <a href="#works" className="focus-ring home-nav-link">
              作品
            </a>
            <Link to="/notes" className="focus-ring home-nav-link">
              手记
            </Link>
            <a href="#about" className="focus-ring home-nav-link">
              关于
            </a>
            <Link
              to={user ? '/workspace/notes' : '/login'}
              className="focus-ring home-account-link"
              title={user ? '进入写作台' : '登录'}
              aria-label={user ? '进入写作台' : '登录'}
            >
              {user ? <PenLine size={17} /> : <LogIn size={17} />}
            </Link>
          </nav>
        </header>

        <div className="home-hero-content">
          <div className="home-eyebrow">
            <span aria-hidden="true" />
            {activeSlide.eyebrow}
          </div>
          <h1>{activeSlide.title}</h1>
          <p>{activeSlide.subtitle}</p>
          <Link to={activeSlide.to} className="focus-ring home-text-link">
            <span>{activeSlide.linkLabel}</span>
            <ArrowRight size={17} />
          </Link>
        </div>

        <div className="home-carousel-controls" aria-label="首页状态轮播">
          {HERO_SLIDES.map((slide, index) => (
            <button
              key={slide.id}
              type="button"
              aria-label={`切换到：${slide.eyebrow}`}
              aria-pressed={index === activeSlideIndex}
              className={index === activeSlideIndex ? 'home-carousel-square is-active' : 'home-carousel-square'}
              onClick={() => setActiveSlideIndex(index)}
            />
          ))}
        </div>
      </section>

      <section id="about" className="home-about">
        <div className="home-section-inner home-about-grid">
          <div className="home-about-copy">
            <div className="home-section-label">关于我</div>
            <h2>记录学习、<br />游戏创作与技术实验。</h2>
            <p>
              WAHT 是我整理想法、验证系统并保存成长过程的个人空间。
              我希望这里展示的不只是结果，也包括每个作品如何一步步成为现在的样子。
            </p>
            <div className="home-handwritten-note">把复杂的系统，做得可玩、可感、可分享。</div>
          </div>

          <div className="home-recent-notes">
            <div className="home-section-label">最近的想法</div>
            {notesQuery.isLoading ? <p className="home-empty-copy">正在整理最近的公开手记...</p> : null}
            {notesQuery.isError ? <p className="home-empty-copy">笔记服务暂时不可用，稍后再回来看看。</p> : null}
            {!notesQuery.isLoading && !notesQuery.isError && recentNotes.length === 0 ? (
              <p className="home-empty-copy">第一篇公开手记还在整理中。</p>
            ) : null}
            <div className="home-note-list">
              {recentNotes.map((note) => (
                <Link key={note.id} to={`/notes/${note.slug}`} className="focus-ring home-note-row">
                  <time dateTime={note.publishedAt ?? undefined}>{formatCompactDate(note)}</time>
                  <span>{note.title}</span>
                  <ArrowRight size={15} />
                </Link>
              ))}
            </div>
            <Link to="/notes" className="focus-ring home-text-link home-all-notes">
              <BookOpen size={16} />
              <span>查看全部手记</span>
            </Link>
          </div>
        </div>
      </section>

      <section id="works" className="home-works">
        <div className="home-section-inner">
          <div className="home-works-heading">
            <div>
              <div className="home-section-label">作品档案</div>
              <h2>从首页选择作品，进入对应的项目页面。</h2>
            </div>
            <Link to="/projects" className="focus-ring home-text-link">
              <span>浏览全部项目</span>
              <ArrowRight size={17} />
            </Link>
          </div>

          <div className="home-archive-grid">
            {archiveEntries.map((entry, index) => (
              <Link
                key={entry.id}
                to={entry.to}
                className="focus-ring home-archive-entry"
                aria-label={`${entry.title}：${entry.actionLabel}`}
              >
                <div
                  className="home-archive-artwork"
                  style={{
                    backgroundImage: `url("${archiveTriptych}")`,
                    backgroundPosition: getArchivePosition(index),
                  }}
                />
                <div className="home-archive-meta">
                  <div>
                    <span className="home-archive-index">0{index + 1}</span>
                    <h3>{entry.title}</h3>
                  </div>
                  <ArrowRight size={18} />
                </div>
                <p>{entry.summary}</p>
                <div className="home-archive-footer">
                  <div className="home-tech-list">
                    {entry.techLabels.map((techLabel) => (
                      <span key={techLabel}>{techLabel}</span>
                    ))}
                  </div>
                  <span>{entry.actionLabel}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <footer className="home-footer">
        <span>WAHT · 个人创作档案</span>
        <Link to={user ? '/workspace/notes' : '/login'} className="focus-ring">
          {user ? <UserRound size={16} /> : <LogIn size={16} />}
          <span>{user ? '进入个人工作台' : '登录个人工作台'}</span>
        </Link>
      </footer>
    </div>
  );
}
