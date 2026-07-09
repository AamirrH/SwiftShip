import { useEffect, useState } from "react";

export function useApiResource(fetcher, fallback, dependencies = []) {
  const [data, setData] = useState(fallback);
  const [status, setStatus] = useState("idle");

  useEffect(() => {
    let active = true;
    setStatus("loading");

    fetcher()
      .then((result) => {
        if (!active) return;
        setData(result);
        setStatus("ready");
      })
      .catch(() => {
        if (!active) return;
        setData(fallback);
        setStatus("fallback");
      });

    return () => {
      active = false;
    };
  }, dependencies);

  return { data, status };
}
