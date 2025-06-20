"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";

export function useAuthRedirect() {
  const router = useRouter();
  const timerRef = useRef<NodeJS.Timeout>(null);

  const handleAuthError = () => {
    timerRef.current = setTimeout(() => {
      router.push("/auth/login");
    }, 2000);
  };

  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  return { handleAuthError };
}
