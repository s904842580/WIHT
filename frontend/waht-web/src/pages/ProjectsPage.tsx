import archiveTriptych from '@/assets/archive-triptych.webp';
import { projectsApi } from '@/api/projects';
import type { ProjectSummary } from '@/types/api';
import { formatDate } from '@/utils/date';
import { getErrorMessage } from '@/utils/error';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, ArrowRight, Loader2 } from 'lucide-react';
import type { CSSProperties } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

function getArtworkPosition(index: number): string {
  if (index % 3 === 0) {
    return '0% center';
  }
  if (index % 3 === 1) {
    return '50% center';
  }
  return '100% center';
}

function getArtworkStyle(index: number): CSSProperties {
  return {
    backgroundImage: `url("${archiveTriptych}")`,
    backgroundPosition: getArtworkPosition(index),
  };
}

function ProjectTechList({ project }: { project: ProjectSummary }) {
  return (
    <div className="project-tech-list">
      {project.techStacks.map((techStack) => (
        <span key={techStack.id}>{techStack.name}</span>
      ))}
    </div>
  );
}

// ProjectsPage 是项目总览：首页可以通过 focus 参数先选中作品，再由这里进入详情。
export function ProjectsPage() {
  const [searchParams] = useSearchParams();
  const focusedSlug = searchParams.get('focus');

  const projectsQuery = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.listProjects,
  });

  const projects = projectsQuery.data ?? [];
  const focusedProject = focusedSlug ? projects.find((project) => project.slug === focusedSlug) : undefined;
  const focusedProjectIndex = focusedProject ? projects.findIndex((project) => project.id === focusedProject.id) : -1;

  return (
    <div className="project-index-page">
      <Link to="/" className="focus-ring project-back-link">
        <ArrowLeft size={16} />
        <span>返回首页</span>
      </Link>

      <header className="project-index-header">
        <div className="home-section-label">Project Archive</div>
        <h1>项目不是一张卡片，<br />而是一段可以展开的创作过程。</h1>
        <p>从首页选中的作品会先在这里展开概览，再进入独立详情页查看技术、记录与演示入口。</p>
      </header>

      {projectsQuery.isLoading ? (
        <div className="project-state">
          <Loader2 className="animate-spin" size={18} />
          <span>正在加载项目档案</span>
        </div>
      ) : null}

      {projectsQuery.isError ? (
        <div className="project-state project-state-error">{getErrorMessage(projectsQuery.error)}</div>
      ) : null}

      {!projectsQuery.isLoading && !projectsQuery.isError && projects.length === 0 ? (
        <div className="project-state">代表项目仍在整理中，这里会保留为空白档案。</div>
      ) : null}

      {focusedSlug && projects.length > 0 && !focusedProject ? (
        <div className="project-state project-state-error">首页选择的项目不存在或尚未发布。</div>
      ) : null}

      {focusedProject ? (
        <section className="project-focus" aria-labelledby="focused-project-title">
          <div className="project-focus-artwork" style={getArtworkStyle(focusedProjectIndex)} />
          <div className="project-focus-copy">
            <div className="home-section-label">从首页选中的作品</div>
            <h2 id="focused-project-title">{focusedProject.name}</h2>
            <p>{focusedProject.summary}</p>
            <ProjectTechList project={focusedProject} />
            <div className="project-focus-footer">
              <span>开始于 {formatDate(focusedProject.startedAt)}</span>
              <Link to={`/projects/${focusedProject.slug}`} className="focus-ring project-detail-link">
                <span>进入项目详情</span>
                <ArrowRight size={17} />
              </Link>
            </div>
          </div>
        </section>
      ) : null}

      {projects.length > 0 ? (
        <section className="project-list-section" aria-labelledby="all-projects-title">
          <div className="project-list-heading">
            <div>
              <div className="home-section-label">全部档案</div>
              <h2 id="all-projects-title">现有项目</h2>
            </div>
            <span>{projects.length.toString().padStart(2, '0')} 个公开项目</span>
          </div>

          <div className="project-editorial-list">
            {projects.map((project, index) => (
              <Link
                key={project.id}
                to={`/projects/${project.slug}`}
                className={project.id === focusedProject?.id ? 'focus-ring project-editorial-row is-focused' : 'focus-ring project-editorial-row'}
              >
                <span className="project-row-index">{(index + 1).toString().padStart(2, '0')}</span>
                <div className="project-row-artwork" style={getArtworkStyle(index)} />
                <div className="project-row-copy">
                  <h3>{project.name}</h3>
                  <p>{project.summary}</p>
                  <ProjectTechList project={project} />
                </div>
                <div className="project-row-action">
                  <span>{formatDate(project.startedAt)}</span>
                  <ArrowRight size={18} />
                </div>
              </Link>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}
