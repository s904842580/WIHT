type SectionHeaderProps = {
  title: string;
  description?: string;
};

// SectionHeader 是页面分区标题，保持各页面标题样式一致。
export function SectionHeader({ title, description }: SectionHeaderProps) {
  return (
    <div className="mb-4">
      <h1 className="text-2xl font-semibold tracking-normal text-ink">{title}</h1>
      {description ? <p className="mt-1 max-w-3xl text-sm leading-6 text-ink/65">{description}</p> : null}
    </div>
  );
}
