import { SectionHeader } from '@/components/SectionHeader';
import { Boxes } from 'lucide-react';

const assetRules = ['只展示 PUBLIC_SAFE 素材', '保留来源说明', '保留授权说明'];

// AssetsPage 展示素材模块规则，避免误用版权不确定的本地素材。
export function AssetsPage() {
  return (
    <div>
      <SectionHeader title="素材展示" description="素材展示只使用已经确认可公开的素材元数据。" />
      <div className="panel p-5">
        <div className="grid gap-4 md:grid-cols-3">
          {assetRules.map((rule) => (
            <div key={rule} className="rounded-lg border border-black/10 bg-paper p-4">
              <Boxes className="text-jade" size={20} />
              <div className="mt-3 text-sm font-medium text-ink">{rule}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
