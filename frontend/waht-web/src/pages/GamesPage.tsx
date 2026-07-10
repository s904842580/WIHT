import { SectionHeader } from '@/components/SectionHeader';
import { Gamepad2 } from 'lucide-react';

const topics = ['角色成长系统', '战斗反馈设计', '关卡节奏分析'];

// GamesPage 是游戏科普入口，第一阶段复用笔记模块承载内容。
export function GamesPage() {
  return (
    <div>
      <SectionHeader title="游戏科普" description="第一阶段先作为学习笔记分类，后续再决定是否独立成模块。" />
      <div className="grid gap-4 md:grid-cols-3">
        {topics.map((topic) => (
          <div key={topic} className="panel p-5">
            <Gamepad2 className="text-vermilion" size={22} />
            <h2 className="mt-4 text-base font-semibold text-ink">{topic}</h2>
            <p className="mt-2 text-sm leading-6 text-ink/60">围绕机制、反馈、数值和体验做结构化记录。</p>
          </div>
        ))}
      </div>
    </div>
  );
}
