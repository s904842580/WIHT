import { projectsApi } from '@/api/projects';
import { SectionHeader } from '@/components/SectionHeader';
import { formatDate } from '@/utils/date';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, ExternalLink, Layers3, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';

// ProjectsPage 展示后端 /api/projects 返回的公开项目列表。
export function ProjectsPage() {
  const projectsQuery = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.listProjects,
  });

  return (
    <div>
      <SectionHeader title="项目展示" description="这里展示已经发布的项目，数据来自 Java 后端公开接口。" />

      {projectsQuery.isLoading ? (
        <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
          <Loader2 className="animate-spin" size={18} />
          <span>正在加载项目</span>
        </div>
      ) : null}

      {projectsQuery.isError ? (
        <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
          {(projectsQuery.error as Error).message}
        </div>
      ) : null}

      {projectsQuery.data?.length === 0 ? <div className="panel p-5 text-sm text-ink/60">暂时没有已发布项目。</div> : null}

      <div className="grid gap-4 md:grid-cols-2">
        {projectsQuery.data?.map((project) => (
          <Link key={project.id} to={`/projects/${project.slug}`} className="focus-ring block rounded-lg">
            <article className="panel h-full p-5 transition hover:-translate-y-0.5 hover:shadow-lg">
              <div className="flex items-start justify-between gap-3">
                <div className="rounded-md bg-gold/10 p-2 text-gold">
                  <Layers3 size={20} />
                </div>
                <ArrowRight size={18} className="text-ink/35" />
              </div>

              <h2 className="mt-4 text-lg font-semibold text-ink">{project.name}</h2>
              <p className="mt-2 min-h-12 text-sm leading-6 text-ink/65">{project.summary}</p>

              <div className="mt-4 flex flex-wrap gap-2">
                {project.techStacks.map((techStack) => (
                  <span key={techStack.id} className="rounded-md border border-black/10 px-2 py-1 text-xs text-ink/70">
                    {techStack.name}
                  </span>
                ))}
              </div>

              <div className="mt-4 flex flex-wrap items-center gap-2 text-xs text-ink/50">
                <span>{formatDate(project.startedAt)}</span>
                <span>·</span>
                <span>{project.links.length} 个链接</span>
                {project.links.length > 0 ? (
                  <>
                    <span>·</span>
                    <span className="inline-flex items-center gap-1">
                      <ExternalLink size={13} />
                      {project.links[0].title}
                    </span>
                  </>
                ) : null}
              </div>
            </article>
          </Link>
        ))}
      </div>
    </div>
  );
}
