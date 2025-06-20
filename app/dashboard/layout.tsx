// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";

import React, { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import {
  AppShell,
  Burger,
  Button,
  Group,
  Menu,
  Tabs,
  Text,
  Box,
  NavLink,
  Image,
  useMantineColorScheme,
} from "@mantine/core";
import { CircleUserRound, LogOut, Settings, Check } from "lucide-react";
import { NAME, setName } from "@/app/global";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import { Home, Presentation, Users, Calendar, Logs } from "lucide-react";
import { SchedulePage } from "./schedule";
import { LogsPage } from "./logs";
import { notifications } from "@mantine/notifications";

export default function Layout({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [opened, setOpened] = useState(false);
  const router = useRouter();
  const { setColorScheme, colorScheme } = useMantineColorScheme();
  const { handleAuthError } = useAuthRedirect();

  const toggle = () => setOpened((o) => !o);
  const root_dir: string = "/dashboard";
  const [activeTab, setActiveTab] = useState<string | null>("Home");
  const data = [
    { icon: Home, label: "Home", page: children, auth: true },
    { icon: Presentation, label: "Courses", page: children, auth: false },
    { icon: Users, label: "Users", page: children, auth: false },
    { icon: Calendar, label: "Schedule", page: <SchedulePage />, auth: true },
    { icon: Logs, label: "Logs", page: <LogsPage />, auth: true },
  ];

  const currentPage = data.find((tab) => tab.label === activeTab)?.page;

  // Fetch User Data
  useEffect(() => {
    const fetchData = async () => {
      try {
        const responseMe = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );

        if (!responseMe.ok) {
          handleAuthError();
          throw new Error("Failed to fetch user information");
        }
        const dataMe = await responseMe.json();
        const userId = dataMe?.data?.id;
        {
          /* Fetch the latest user information */
        }
        const responseUser = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/users/${userId}`,
          {
            credentials: "include",
          },
        );
        if (!responseUser.ok) {
          throw new Error("Failed to fetch user information");
        }
        const userData = await responseUser.json();
        setUser(userData?.data);
        setName(userData?.data.username);
        setColorScheme(userData?.data.userTheme === "DARK" ? "dark" : "light");
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleGoSetting = async () => {
    const id = user?.id;
    if (id) {
      setActiveTab("Home");
      router.push(`/dashboard/${id}`);
    }
  };

  const handleChangeValue = async (value: string | null) => {
    setActiveTab(value);
    if (!value) return;

    // Navigate to the corresponding route based on the tab
    if (value === "Schedule") {
      router.push(`${root_dir}/schedule`);
    } else if (value === "Logs") {
      router.push(`${root_dir}/logs`);
    } else if (value === "Courses") {
      router.push(`${root_dir}/courses`);
    } else if (value === "Users") {
      router.push(`${root_dir}/users`);
    } else {
      router.push(root_dir); // Default to Home
    }
  };

  const handleAdminManage = async (value: string | null) => {
    if (!value) return;

    // Navigate to admin pages based on the tab
    if (value === "Courses") {
      router.push(`${root_dir}/courses`);
    } else if (value === "Users") {
      router.push(`${root_dir}/users`);
    } else if (value === "Schedule") {
      router.push(`${root_dir}/schedule`);
    } else if (value === "Logs") {
      router.push(`${root_dir}/logs`);
    } else if (value === "Home") {
      router.push(root_dir);
    } else {
      notifications.show({
        id: "error",
        title: "Error",
        message: "Invalid operation",
        color: "red",
        autoClose: 2000,
      });
    }
  };

  const logout = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/logout`,
        {
          method: "POST",
          credentials: "include",
        },
      );

      if (!response.ok) throw new Error("Logout failed");

      notifications.show({
        id: "logout",
        title: "Logout successful",
        message:
          "You have been successfully logged out. Redirecting to the login page...",
        color: "lime",
        icon: <Check size={16} />,
        autoClose: 1000,
        onClose: () => {
          router.push("/auth/login");
        },
      });
      setColorScheme("light");
      setLoading(true);
    } catch (error: any) {
      setError(error.message);
      notifications.show({
        id: "error",
        title: "Error",
        message: error.message || "Logout failed",
        color: "red",
        autoClose: 1000,
      });
      router.push("/auth/login");
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <AppShell header={{ height: 80 }} padding={{ base: 10, sm: 15, lg: "xl" }}>
      <AppShell.Header
        style={{
          position: "fixed",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          padding: "0 20px",
        }}
      >
        <Group gap="xs" aria-label="Header group">
          <Burger
            opened={opened}
            onClick={toggle}
            hiddenFrom="sm"
            size="sm"
            aria-label="Toggle navigation"
          />
          {colorScheme === "dark" ? (
            <Image src="/Logo_dark.png" w={180} aria-label="Logo dark image" />
          ) : (
            <Image src="/Logo.png" w={180} aria-label="Logo image" />
          )}
        </Group>

        <Tabs
          color="indigo"
          defaultValue="Home"
          variant="pills"
          value={activeTab}
          onChange={(value) => handleChangeValue(value)}
          style={{ flex: 1, maxWidth: 600 }}
          visibleFrom="sm"
          aria-label="Main navigation tabs"
        >
          <Tabs.List aria-label="Tabs list">
            {data.map((item) => (
              <Tabs.Tab
                size="xl"
                key={item.label}
                onClick={() => handleAdminManage(item.label)}
                value={item.label}
                leftSection={
                  <item.icon size={18} aria-label={`${item.label} icon`} />
                }
                color="indigo"
                hidden={!item.auth && user?.role !== "ADMIN"}
                aria-label={`${item.label} tab`}
              >
                {item.label === "Home"
                  ? "Home"
                  : item.label === "Courses"
                    ? "Courses"
                    : item.label === "Users"
                      ? "Users"
                      : item.label === "Schedule"
                        ? "Schedule"
                        : item.label === "Logs"
                          ? "Logs"
                          : item.label}
              </Tabs.Tab>
            ))}
          </Tabs.List>
        </Tabs>

        <Menu shadow="md" width={250} aria-label="User menu">
          <Menu.Target>
            <Button
              variant={colorScheme === "dark" ? "filled" : "outline"}
              size="md"
              leftSection={<CircleUserRound size={20} aria-label="User icon" />}
              aria-label="User menu button"
            >
              {NAME}
            </Button>
          </Menu.Target>

          <Menu.Dropdown aria-label="User menu dropdown">
            <Menu.Label aria-label="Settings label">Settings</Menu.Label>
            <Menu.Item
              leftSection={<Settings size={16} aria-label="Settings icon" />}
              onClick={handleGoSetting}
              aria-label="Personal Settings menu item"
            >
              Personal Settings
            </Menu.Item>
            <Menu.Divider aria-label="Menu divider" />
            <Menu.Label aria-label="Dangerous Operations label">
              Dangerous Operations
            </Menu.Label>
            <Menu.Item
              color="red"
              leftSection={<LogOut size={16} aria-label="Logout icon" />}
              onClick={logout}
              aria-label="Logout menu item"
            >
              Logout
            </Menu.Item>
          </Menu.Dropdown>
        </Menu>
      </AppShell.Header>

      <AppShell.Navbar p="md" hidden={!opened} aria-label="Sidebar navigation">
        <Box aria-label="Sidebar nav box">
          {data.map((item) => (
            <NavLink
              key={item.label}
              label={
                item.label === "Home"
                  ? "Home"
                  : item.label === "Courses"
                    ? "Courses"
                    : item.label === "Users"
                      ? "Users"
                      : item.label === "Schedule"
                        ? "Schedule"
                        : item.label === "Logs"
                          ? "Logs"
                          : item.label
              }
              leftSection={
                <item.icon size={18} aria-label={`${item.label} icon`} />
              }
              active={activeTab === item.label}
              onClick={() => {
                setActiveTab(item.label);
                handleAdminManage(item.label);
                setOpened(false);
              }}
              variant="subtle"
              style={{ borderRadius: 8, marginBottom: 4 }}
              hidden={!item.auth && user?.role !== "ADMIN"}
              aria-label={`${item.label} sidebar nav link`}
            />
          ))}
        </Box>
      </AppShell.Navbar>

      <AppShell.Main
        style={{
          paddingTop: 100,
          minHeight: "calc(100vh - 80px)",
          position: "relative",
        }}
        aria-label="Main content area"
      >
        {currentPage}
      </AppShell.Main>
    </AppShell>
  );
}
