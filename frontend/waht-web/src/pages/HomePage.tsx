import { SectionHeader } from '@/components/SectionHeader';
import { ArrowRight, BookOpen, Database, Layers3, ShieldCheck } from 'lucide-react';
import { Link } from 'react-router-dom';

const quickStats = [
  { label: '后端状态', value: 'Spring Boot 3', icon: ShieldCheck },
  { label: '数据层', value: 'MyBatis-Plus', icon: Database },
  { label: '内容主线', value: '学习笔记', icon: BookOpen },
];

const recentNotes = [
  'Spring Boot 登录注册闭环',
  'MySQL 初始化脚本设计',
  'React 前台工程搭建',
];

// HomePage 是首页，先展示当前项目状态和内容入口。
export function HomePage() {
  return (
    <div className="space-y-6">
      <section className="grid gap-4 lg:grid-cols-[1.4fr_0.8fr]">
        <div className="panel overflow-hidden">
          <div className="grid gap-4 p-5 md:grid-cols-[1fr_220px]">
            <div>
              <SectionHeader
                title="WAHT 工作台"
                description="这里会逐步沉淀 Java 后端、数据库设计、学习笔记、项目展示和游戏系统科普内容。"
              />
              <div className="flex flex-wrap gap-2">
                <Link
                  to="/notes"
                  className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-ink px-3 text-sm font-medium text-white hover:bg-ink/90"
                >
                  <BookOpen size={16} />
                  <span>查看笔记</span>
                </Link>
                <Link
                  to="/projects"
                  className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 px-3 text-sm font-medium text-ink hover:bg-black/5"
                >
                  <Layers3 size={16} />
                  <span>项目展示</span>
                </Link>
              </div>
            </div>

            <div className="rounded-lg border border-black/10 bg-[linear-gradient(135deg,#fff7ed,#ecfeff)] p-4">
              <div className="text-sm font-medium text-ink/70">当前闭环</div>
              <div className="mt-3 space-y-3">
                {['MySQL 初始化', 'Java 登录接口', 'React 登录联调'].map((item) => (
                  <div key={item} className="flex items-center gap-2 text-sm text-ink">
                    <span className="h-2.5 w-2.5 rounded-full bg-jade" />
                    <span>{item}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="panel p-5">
          <div className="text-sm font-medium text-ink/70">最近笔记</div>
          <div className="mt-4 space-y-3">
            {recentNotes.map((note) => (
              <Link
                key={note}
                to="/notes"
                className="focus-ring flex items-center justify-between rounded-md border border-black/10 px-3 py-2 text-sm text-ink hover:bg-black/5"
              >
                <span>{note}</span>
                <ArrowRight size={16} />
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        {quickStats.map((item) => {
          const Icon = item.icon;
          return (
            <div key={item.label} className="panel p-5">
              <Icon className="text-vermilion" size={22} />
              <div className="mt-4 text-sm text-ink/60">{item.label}</div>
              <div className="mt-1 text-lg font-semibold text-ink">{item.value}</div>
            </div>
          );
        })}
      </section>
    </div>
  );
}
