"use client";
import { Button, Stack } from "@mantine/core";
import { Settings } from "./settings";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { useColorScheme } from "@mantine/hooks";

export function UserClient(props: { userId: number }) {
  const router = useRouter();
  const colorScheme = useColorScheme();

  return (
    <Stack gap="md" align="flex-start" p="md">
      <Button
        variant="subtle"
        color={colorScheme === "dark" ? "white" : "gray"}
        onClick={() => router.back()}
        leftSection={<ArrowLeft size={18} />}
        aria-label="Back button"
      >
        Back
      </Button>
      <Settings userId={props.userId} />
    </Stack>
  );
}
