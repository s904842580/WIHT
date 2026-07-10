import { request } from '@/api/http';
import type { ProjectDetail, ProjectSummary, TechStack } from '@/types/api';

// projectsApi 只放项目展示公开查询接口。
export const projectsApi = {
  listProjects() {
    return request<ProjectSummary[]>('/projects', {
      auth: false,
    });
  },

  getProject(slug: string) {
    return request<ProjectDetail>(`/projects/${slug}`, {
      auth: false,
    });
  },

  listTechStacks() {
    return request<TechStack[]>('/tech-stacks', {
      auth: false,
    });
  },
};
