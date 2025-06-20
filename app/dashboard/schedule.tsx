// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Button,
  Card,
  Drawer,
  Grid,
  Group,
  Modal,
  Text,
  TextInput,
  Title,
  useMantineColorScheme,
} from "@mantine/core";
import { Calendar } from "@mantine/dates";
import { useDisclosure } from "@mantine/hooks";
import { Calendar as IconCalendar, Plus as IconPlus } from "lucide-react";
import dayjs from "dayjs";
import { Assignment, ScheduleEntry } from "@/app/interface";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

export function SchedulePage() {
  const [assignments, setAssignments] = useState<{ data: Assignment[] } | null>(
    null,
  );
  const [scheduleEntries, setScheduleEntries] = useState<ScheduleEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [newEntry, setNewEntry] = useState({
    title: "",
    date: new Date(),
    description: "",
  });
  const [modalOpened, { open: openModal, close: closeModal }] =
    useDisclosure(false);
  const [drawerOpened, { open: openDrawer, close: closeDrawer }] =
    useDisclosure(false);
  const router = useRouter();
  const { colorScheme } = useMantineColorScheme();
  const { handleAuthError } = useAuthRedirect();

  const getAssignmentsByDate = () => {
    const map = new Map<string, Assignment[]>();
    assignments?.data.forEach((assignment) => {
      const dueDate = new Date(assignment.dueDate).toISOString().split("T")[0];
      if (!map.has(dueDate)) {
        map.set(dueDate, []);
      }
      map.get(dueDate)!.push(assignment);
    });
    return map;
  };

  const getSchedulesByDate = () => {
    const map = new Map<string, ScheduleEntry[]>();
    scheduleEntries.forEach((entry) => {
      const entryDate = new Date(entry.date).toISOString().split("T")[0];
      if (!map.has(entryDate)) {
        map.set(entryDate, []);
      }
      map.get(entryDate)!.push(entry);
    });
    return map;
  };

  const assignmentsByDate = getAssignmentsByDate();
  const schedulesByDate = getSchedulesByDate();

  // Fetch current user ID
  const fetchUserId = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
        {
          credentials: "include",
        },
      );
      if (!response.ok) throw new Error("Failed to fetch user information");
      const data = await response.json();
      return data.data.id;
    } catch (err: any) {
      setError(err.message);
      return null;
    }
  };

  // Fetch schedules
  const fetchSchedules = async (userId: number) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/schedules?user-id=${userId}`,
        { credentials: "include" },
      );
      if (!response.ok) throw new Error("Failed to fetch schedules");
      const data = await response.json();
      const schedules: ScheduleEntry[] = data.data.map((schedule: any) => ({
        id: schedule.id,
        title: schedule.title,
        date: new Date(schedule.time), // Backend returns the 'time' field
        description: schedule.description || "",
      }));
      setScheduleEntries(schedules);
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Fetch data (assignments and schedules)
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const userId = await fetchUserId();
      if (!userId) return;

      // Fetch courses and assignments
      const responseCourseUsers = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/course-users?user-id=${userId}`,
        { method: "GET", credentials: "include" },
      );
      if (!responseCourseUsers.ok)
        throw new Error("Failed to fetch course information");
      const courseUsers = await responseCourseUsers.json();
      const userCourseIds: number[] = courseUsers.data.map(
        (cu: any) => cu.course.id,
      );

      const responseAssignments = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/assignments?user-id=${userId}`,
        { credentials: "include" },
      );
      if (!responseAssignments.ok)
        throw new Error("Failed to fetch assignment information");
      const assignmentsData = await responseAssignments.json();

      const filteredAssignments = {
        data: (assignmentsData.data || [])
          .filter((assignment: Assignment) =>
            userCourseIds.includes(assignment.course.id),
          )
          .sort(
            (a: Assignment, b: Assignment) =>
              new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime(),
          ),
      };
      setAssignments(filteredAssignments);

      // Fetch schedules
      await fetchSchedules(userId);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Add schedule entry
  const handleAddEntry = async () => {
    if (!newEntry.title || !newEntry.date) return;

    try {
      const userId = await fetchUserId();
      if (!userId) return;

      const schedule = {
        title: newEntry.title,
        time: newEntry.date.toISOString().replace(/\.\d{3}Z$/, ""), // Change to 'time'
        description: newEntry.description,
        user: { id: userId },
      };

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/schedules`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(schedule),
          credentials: "include",
        },
      );

      if (!response.ok) throw new Error("Failed to save schedule");

      const result = await response.json();
      const newSchedule: ScheduleEntry = {
        id: result.data.id,
        title: newEntry.title,
        date: newEntry.date,
        description: newEntry.description,
      };

      setScheduleEntries([...scheduleEntries, newSchedule]);
      setNewEntry({ title: "", date: new Date(), description: "" });
      closeModal();
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getDayProps = (date: string) => {
    const dayAssignments = assignmentsByDate.get(date) || [];
    const daySchedules = schedulesByDate.get(date) || [];

    let title = "";
    if (dayAssignments.length > 0) {
      title += `Assignment Due:\n${dayAssignments
        .map((a) => `${a.course.name}: ${a.title}`)
        .join("\n")}`;
    }
    if (daySchedules.length > 0) {
      if (title) title += "\n\n"; // Add spacing between assignments and schedules
      title += `Schedule:\n${daySchedules.map((s) => s.title).join("\n")}`;
    }

    return {
      style: {
        backgroundColor:
          dayAssignments.length > 0
            ? colorScheme === "dark"
              ? "rgba(0,255,218,0.44)"
              : "rgba(0,51,255,0.49)"
            : daySchedules.length > 0
              ? colorScheme === "dark"
                ? "rgba(255,204,0,0.38)"
                : "rgba(255,137,0,0.44)"
              : undefined,
      },
      title,
    };
  };

  if (loading)
    return <div aria-label="Loading schedules">Loading schedules...</div>;
  if (error) return <div aria-label="Error message">Error: {error}</div>;

  return (
    <div aria-label="Schedule Page container">
      <Group justify="space-between" mb="lg" aria-label="Header group">
        <Title order={2} aria-label="Page title">
          My Schedule
        </Title>
        <Button
          leftSection={<IconPlus size={18} aria-label="Add icon" />}
          onClick={openModal}
          variant={colorScheme === "dark" ? "filled" : "outline"}
          color="green"
          aria-label="Add Schedule button"
        >
          Add Schedule
        </Button>
      </Group>

      <Grid aria-label="Main grid">
        <Grid.Col span={{ base: 12, md: 4 }} aria-label="Calendar column">
          <Card
            shadow="sm"
            padding="lg"
            radius="md"
            withBorder
            aria-label="Calendar card"
          >
            <Button
              fullWidth
              leftSection={
                <IconCalendar size={18} aria-label="Calendar icon" />
              }
              onClick={openDrawer}
              variant={colorScheme === "dark" ? "filled" : "outline"}
              aria-label="View Calendar button"
            >
              View Calendar
            </Button>
          </Card>
        </Grid.Col>
        <Grid.Col
          span={{ base: 12, md: 8 }}
          aria-label="Assignments and schedules column"
        >
          <Card
            shadow="sm"
            padding="lg"
            radius="md"
            withBorder
            aria-label="Assignments and schedules card"
          >
            <Text
              fw={600}
              mb="md"
              aria-label="Upcoming Assignment Deadlines label"
            >
              Upcoming Assignment Deadlines
            </Text>
            {assignments?.data.length === 0 && (
              <Text
                c="dimmed"
                aria-label="No upcoming assignment deadlines label"
              >
                No upcoming assignment deadlines.
              </Text>
            )}
            {Array.from(assignmentsByDate.entries()).map(
              ([dateString, assignments]) => (
                <div
                  key={dateString}
                  aria-label={`Assignment date group: ${new Date(dateString).toLocaleDateString()}`}
                >
                  <Text fw={500} c="red" mb="xs" aria-label="Assignment date">
                    {new Date(dateString).toLocaleDateString()}
                  </Text>
                  {assignments.map((assignment) => (
                    <Group
                      key={assignment.id}
                      mb="xs"
                      wrap="wrap"
                      aria-label={`Assignment group: ${assignment.title}`}
                    >
                      <Text aria-label="Assignment course and title">
                        {assignment.course.name} - {assignment.title}
                      </Text>
                      <Text c="dimmed" aria-label="Assignment due time">
                        Due Time:{" "}
                        {new Date(assignment.dueDate).toLocaleTimeString()}
                      </Text>
                    </Group>
                  ))}
                </div>
              ),
            )}
            <Text fw={600} mt="lg" mb="md" aria-label="My Schedule label">
              My Schedule
            </Text>
            {scheduleEntries.length === 0 && (
              <Text c="dimmed" aria-label="No schedules label">
                No schedules.
              </Text>
            )}
            {scheduleEntries.map((entry) => (
              <Group
                key={entry.id}
                mb="xs"
                wrap="wrap"
                aria-label={`Schedule entry group: ${entry.title}`}
              >
                <Text aria-label="Schedule entry title">{entry.title}</Text>
                <Text c="blue" aria-label="Schedule entry date">
                  Date: {entry.date.toLocaleDateString()}
                </Text>
                {entry.description && (
                  <Text c="dimmed" aria-label="Schedule entry description">
                    {entry.description}
                  </Text>
                )}
              </Group>
            ))}
          </Card>
        </Grid.Col>
      </Grid>

      <Drawer
        opened={drawerOpened}
        onClose={closeDrawer}
        title="Schedule Calendar"
        size="md"
        position="right"
        styles={{
          content: { display: "flex", flexDirection: "column" },
          body: { flex: 1, padding: "20px" },
          root: {
            "--drawer-size": "100%",
            "@media (minWidth: 768px)": {
              "--drawer-size": "400px",
            },
          },
        }}
        aria-label="Schedule Calendar drawer"
      >
        <Calendar
          size="lg"
          getDayProps={(date) => getDayProps(date)}
          highlightToday
          styles={{
            month: {
              width: "100%",
              maxWidth: "350px",
              margin: "0 auto",
            },
            day: {
              height: "40px",
              fontSize: "16px",
            },
          }}
          aria-label="Calendar component"
        />
      </Drawer>

      <Modal
        opened={modalOpened}
        onClose={closeModal}
        title="Add Schedule"
        aria-label="Add Schedule modal"
      >
        <TextInput
          label="Title"
          placeholder="e.g., Exam Review"
          value={newEntry.title}
          onChange={(e) => setNewEntry({ ...newEntry, title: e.target.value })}
          required
          mb="md"
          aria-label="Title input"
        />
        <TextInput
          label="Date"
          type="date"
          value={newEntry.date.toISOString().split("T")[0]}
          onChange={(e) =>
            setNewEntry({ ...newEntry, date: new Date(e.target.value) })
          }
          required
          mb="md"
          aria-label="Date input"
        />
        <TextInput
          label="Description"
          placeholder="Optional details"
          value={newEntry.description}
          onChange={(e) =>
            setNewEntry({ ...newEntry, description: e.target.value })
          }
          mb="md"
          aria-label="Description input"
        />
        <Group justify="flex-end" aria-label="Modal button group">
          <Button
            variant="outline"
            onClick={closeModal}
            aria-label="Cancel button"
          >
            Cancel
          </Button>
          <Button onClick={handleAddEntry} aria-label="Save button">
            Save
          </Button>
        </Group>
      </Modal>
    </div>
  );
}
