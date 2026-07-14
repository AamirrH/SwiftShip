import { useEffect, useState } from "react";

export function useApiResource(fetcher, fallback, dependencies = []) {
  const [data, setData] = useState(fallback);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    setStatus("loading");
    setError(null);

    fetcher()
      .then((result) => {
        if (!active) return;
        setData(result);
        setStatus("ready");
      })
      .catch((caughtError) => {
        if (!active) return;
        setError(caughtError);
        setData(fallback);
        setStatus("fallback");
      });

    return () => {
      active = false;
    };
  }, dependencies);

  return { data, status, error };
}
