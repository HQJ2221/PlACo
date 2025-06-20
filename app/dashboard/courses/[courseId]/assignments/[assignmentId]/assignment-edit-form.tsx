// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
import { useState, useEffect } from "react";
import {
  Modal,
  NumberInput,
  Button,
  Group,
  Checkbox,
  useMantineColorScheme,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { DateTimePicker } from "@mantine/dates";
import { Assignment, AssignmentEditModalProps } from "@/app/interface";

export function AssignmentEditModal({
  assignment,
  opened,
  onClose,
  onSubmit,
}: AssignmentEditModalProps) {
  const [loading, setLoading] = useState(false);
  const { colorScheme } = useMantineColorScheme();

  const form = useForm<Assignment>({
    initialValues: {
      id: assignment.id,
      title: assignment.title,
      description: assignment.description,
      type: assignment.type,
      publishTime: new Date(assignment.publishTime + "Z"),
      dueDate: new Date(assignment.dueDate + "Z"),
      createTime: new Date(assignment.createTime),
      fullMark: assignment.fullMark,
      course: { id: assignment.course.id, name: assignment.course.name },
      needOCR: assignment.needOCR,
    },

    validate: {
      publishTime: (value) => (value ? null : "Choose publish time"),
      dueDate: (value) => (value ? null : "Choose due date"),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      setLoading(true);
      const payload = {
        ...values,
      };
      await onSubmit(payload);
      onClose();
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title="Modify Assignment Info"
      size="lg"
      overlayProps={{
        opacity: 0.55,
        blur: 3,
      }}
      aria-label="Assignment edit modal"
    >
      <form
        onSubmit={form.onSubmit(handleSubmit)}
        aria-label="Assignment edit form"
      >
        <DateTimePicker
          label="Publish Time"
          placeholder="Choose publish time"
          value={form.values.publishTime}
          onChange={(date) => form.setFieldValue("publishTime", new Date(date))}
          withSeconds={false}
          mt="md"
          error={form.errors.publishTime}
          aria-label="Publish time picker"
        />

        <DateTimePicker
          label="Due Time"
          placeholder="Choose due time"
          value={form.values.dueDate}
          onChange={(date) => form.setFieldValue("dueDate", new Date(date))}
          withSeconds={false}
          mt="md"
          minDate={form.values.publishTime}
          error={form.errors.dueDate}
          aria-label="Due time picker"
        />

        <NumberInput
          label="Full score"
          min={0}
          max={1000}
          mt="md"
          {...form.getInputProps("fullMark")}
          aria-label="Full score input"
        />

        <Checkbox
          label="Enable OCR for this assignment"
          mt="md"
          {...form.getInputProps("needOCR", { type: "checkbox" })}
          aria-label="Enable OCR checkbox"
        />

        <Group mt="xl" aria-label="Form button group">
          <Button
            variant="default"
            onClick={onClose}
            aria-label="Cancel button"
          >
            Cancel
          </Button>
          <Button
            variant={colorScheme === "dark" ? "filled" : "outline"}
            type="submit"
            loading={loading}
            aria-label="Save button"
          >
            Save
          </Button>
        </Group>
      </form>
    </Modal>
  );
}
