"use client";
import { MembersDetails } from "./members-details";
import { Settings } from "./settings";
import { Tabs } from "@mantine/core";

export function CourseClient(props: { courseId: number }) {
  return (
    <Tabs defaultValue="members">
      <Tabs.List>
        <Tabs.Tab value="members">Members</Tabs.Tab>
        <Tabs.Tab value="settings">Settings</Tabs.Tab>
      </Tabs.List>

      <Tabs.Panel value="members">
        <MembersDetails courseId={props.courseId} />
      </Tabs.Panel>

      <Tabs.Panel value="settings">
        <Settings courseId={props.courseId} />
      </Tabs.Panel>
    </Tabs>
  );
}
