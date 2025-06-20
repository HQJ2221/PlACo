"use client";

import { Card, SemiCircleProgress, Stack, Title } from "@mantine/core";
import React from "react";

export function ScoreCard(props: {
  realScore: number;
  fullMark: number;
  hidden: boolean;
  scoreAvailable: boolean;
  width: string;
}) {
  if (props.hidden) {
    return null;
  }
  return (
    /* 显示作业分数（仅非代码作业显示）*/
    <Card
      shadow="sm"
      radius="md"
      withBorder
      w={props.width}
      aria-label="Score card"
    >
      <Stack
        gap="sm"
        justify="center"
        align="center"
        aria-label="Score card stack"
      >
        <Title order={4} aria-label="Score card title">
          SCORE({props.fullMark})
        </Title>
        <SemiCircleProgress
          fillDirection="left-to-right"
          orientation="up"
          filledSegmentColor={
            props.scoreAvailable
              ? "var(--mantine-color-green-6)"
              : "var(--mantine-color-blue-6)"
          }
          emptySegmentColor={
            props.scoreAvailable
              ? "var(--mantine-color-red-5)"
              : "var(--mantine-color-blue-6)"
          }
          size={120}
          thickness={15}
          value={
            props.scoreAvailable ? (props.realScore / props.fullMark) * 100 : 0
          }
          label={props.scoreAvailable ? props.realScore.toString() : "--"}
          aria-label="Score progress"
        />
      </Stack>
    </Card>
  );
}
