import { projectsApi } from '@/api/projects';
import { SectionHeader } from '@/components/SectionHeader';
import { formatDate } from '@/utils/date';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, ExternalLink, Loader2 } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';

// ProjectDetailPage 展示单个公开项目详情，对接 /api/projects/{slug}。
export function ProjectDetailPage() {
  const { slug } = useParams();
  const projectQuery = useQuery({
    queryKey: ['projects', slug],
    queryFn: () => projectsApi.getProject(slug!),
    enabled: Boolean(slug),
  });

  if (projectQuery.isLoading) {
    return (
      <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
        <Loader2 className="animate-spin" size={18} />
        <span>正在加载项目详情</span>
      </div>
    );
  }

  if (projectQuery.isError) {
    return (
      <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
        {(projectQuery.error as Error).message}
      </div>
    );
  }

  const project = projectQuery.data;
  if (!project) {
    return <div className="panel p-5 text-sm text-ink/60">项目不存在。</div>;
  }

  return (
    <article className="space-y-4">
      <Link to="/projects" className="focus-ring inline-flex h-10 items-center gap-2 rounded-md px-2 text-sm text-ink/70 hover:bg-black/5">
        <ArrowLeft size={16} />
        <span>返回项目列表</span>
      </Link>

      <div className="panel p-5">
        <SectionHeader title={project.name} description={project.summary ?? undefined} />
        <div className="flex flex-wrap items-center gap-2 text-xs text-ink/55">
          <span>开始于 {formatDate(project.startedAt)}</span>
          {project.endedAt ? (
            <>
              <span>·</span>
              <span>结束于 {formatDate(project.endedAt)}</span>
            </>
          ) : null}
        </div>
        <div className="mt-3 flex flex-wrap gap-2">
          {project.techStacks.map((techStack) => (
            <span key={techStack.id} className="rounded-md border border-black/10 px-2 py-1 text-xs text-ink/70">
              {techStack.name}
            </span>
          ))}
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_260px]">
        <div className="panel p-5">
          <div className="whitespace-pre-wrap text-sm leading-7 text-ink/80">{project.description}</div>
        </div>

        <aside className="panel h-fit p-5">
          <div className="text-sm font-medium text-ink/70">项目链接</div>
          <div className="mt-4 space-y-2">
            {project.links.length === 0 ? <div className="text-sm text-ink/50">暂无链接。</div> : null}
            {project.links.map((link) => (
              <a
                key={link.id}
                href={link.url}
                className="focus-ring flex items-center justify-between rounded-md border border-black/10 px-3 py-2 text-sm text-ink hover:bg-black/5"
                target={link.url.startsWith('http') ? '_blank' : undefined}
                rel={link.url.startsWith('http') ? 'noreferrer' : undefined}
              >
                <span>{link.title}</span>
                <ExternalLink size={15} className="text-ink/40" />
              </a>
            ))}
          </div>
        </aside>
      </div>
    </article>
  );
}
