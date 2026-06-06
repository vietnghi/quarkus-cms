import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

const STAGES = ["draft", "review", "approved", "published"];

export const WorkflowControl: React.FC = () => {
  const { plural = "articles", id } = useParams<{ plural: string; id?: string }>();
  const [stage, setStage] = useState("draft");

  useEffect(() => {
    if (!id) return;
    fetch(`/cms-admin/api/content-types/${plural}/entries/${id}`)
      .then(r => r.json()).then(j => setStage(j.data?.status ?? "draft"));
  }, [id, plural]);

  const transition = async (toStage: string) => {
    await fetch(`/cms-admin/api/content-types/${plural}/entries/${id}`, {
      method: "PUT", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: toStage }),
    });
    setStage(toStage);
  };

  if (!id) return null;

  return (
    <div style={{ margin: "16px 0", padding: 12, background: "#e8f0fe", borderRadius: 8 }}>
      <strong>Workflow</strong>
      <div style={{ display: "flex", gap: 4, marginTop: 8 }}>
        {STAGES.map(s => (
          <button key={s} onClick={() => transition(s)}
            style={{ padding: "4px 12px", borderRadius: 4, border: "1px solid #ccc",
              background: stage === s ? "#1a73e8" : "#fff", color: stage === s ? "#fff" : "#333", cursor: "pointer" }}>
            {s}
          </button>
        ))}
      </div>
    </div>
  );
};
