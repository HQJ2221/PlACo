// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug

"use client";

import { useEffect, useState } from "react";
import {
  Stack,
  Group,
  Title,
  Input,
  Button,
  Switch,
  Text,
  Loader,
  Alert,
  PasswordInput,
  Divider,
  useMantineColorScheme,
  Checkbox,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { setName } from "@/app/global";

export function UserClient(props: { userId: number }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [user, setUser] = useState<any>(null);
  const [editMode, setEditMode] = useState(false);
  const [newUsername, setNewUsername] = useState("");
  const [passwordData, setPasswordData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const { setColorScheme, colorScheme } = useMantineColorScheme();

  // 获取用户数据
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          { credentials: "include" },
        );

        if (!response.ok) throw new Error("Failed to fetch user data");

        const data = await response.json();
        const userId = data?.data.id;
        if (userId.toString() !== props.userId) {
          throw new Error("Access denied");
        }

        {
          /* 获取最新用户信息 */
        }
        const responseUser = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/users/${userId}`,
          { credentials: "include" },
        );
        if (!responseUser.ok) throw new Error("Failed to fetch user data");
        const dataUser = await responseUser.json();
        setUser(dataUser.data);
        setNewUsername(dataUser.data.username);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, []);

  // 更新用户名
  const handleUsernameUpdate = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ username: newUsername }),
        },
      );

      if (!response.ok) throw new Error("Update failed");

      setUser({ ...user, username: newUsername });
      setEditMode(false);
      notifications.show({
        title: "Success",
        message: "Username updated successfully",
        color: "green",
      });
      setName(newUsername);
    } catch (err: any) {
      notifications.show({
        title: "Failed",
        message: err.message,
        color: "red",
      });
    }
  };

  // 更新密码
  const handlePasswordUpdate = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      notifications.show({
        title: "Failed",
        message: "Passwords do not match",
        color: "red",
      });
      return;
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ password: passwordData.newPassword }),
        },
      );

      if (!response.ok) throw new Error("Password update failed");

      setPasswordData({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      notifications.show({
        title: "Success",
        message: "Password updated successfully",
        color: "green",
      });
    } catch (err: any) {
      notifications.show({
        title: "Failed",
        message: err.message,
        color: "red",
      });
    }
  };

  // 切换主题
  const toggleTheme = async () => {
    setColorScheme(colorScheme === "dark" ? "light" : "dark");
    const targetTheme = colorScheme === "dark" ? 1 : 2;
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            userTheme: targetTheme,
          }),
        },
      );

      if (!response.ok) throw new Error("Theme update failed");
      notifications.show({
        title: "Success",
        message: "Theme updated successfully",
        color: "green",
        autoClose: 2000,
      });
    } catch (error) {
      notifications.show({
        title: "Failed",
        message: "Theme update failed",
        color: "red",
        autoClose: 2000,
      });
    }
  };

  if (error) {
    return (
      <Alert color="red" aria-label="Error alert">
        {error}
      </Alert>
    );
  }

  if (loading) {
    return <Loader aria-label="Loading user data" />;
  }

  return (
    <Stack
      align="flex-start"
      gap="lg"
      w="70%"
      style={{ paddingLeft: "10%" }}
      aria-label="User settings stack"
    >
      {/* 用户信息标题 */}
      <Title c="indigo" order={2} aria-label="User settings title">
        User Settings
      </Title>

      {/* 用户名修改部分 */}
      <Title order={4} c="indigo" aria-label="User name section title">
        User Name
      </Title>
      <Group align="flex-end" w="70%" aria-label="User name group">
        {editMode ? (
          <>
            <Input.Wrapper
              label="Username"
              w="70%"
              aria-label="Username input wrapper"
            >
              <Input
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                aria-label="Username input"
              />
            </Input.Wrapper>
            <Group gap="xs" aria-label="Edit mode button group">
              <Button
                onClick={handleUsernameUpdate}
                aria-label="Save username button"
              >
                Save
              </Button>
              <Button
                variant="outline"
                onClick={() => setEditMode(false)}
                aria-label="Cancel edit button"
              >
                Cancel
              </Button>
            </Group>
          </>
        ) : (
          <>
            <Input.Wrapper
              label="Username"
              w="70%"
              aria-label="Username input wrapper"
            >
              <Input
                value={user?.username}
                readOnly
                aria-label="Username readonly input"
              />
            </Input.Wrapper>
            <Button
              onClick={() => setEditMode(true)}
              aria-label="Edit username button"
            >
              Edit
            </Button>
          </>
        )}
      </Group>

      {/* 密码修改部分 */}
      <Stack w="50%" gap="xs" aria-label="Password section stack">
        <Title order={4} c="indigo" aria-label="Password section title">
          Password
        </Title>

        {/*<Input.Wrapper label="Current Password">*/}
        {/*  <PasswordInput*/}
        {/*    value={passwordData.oldPassword}*/}
        {/*    onChange={(e) => setPasswordData({...passwordData, oldPassword: e.target.value})}*/}
        {/*  />*/}
        {/*</Input.Wrapper>*/}

        <Input.Wrapper
          label="New Password"
          aria-label="New password input wrapper"
        >
          <PasswordInput
            value={passwordData.newPassword}
            onChange={(e) =>
              setPasswordData({ ...passwordData, newPassword: e.target.value })
            }
            aria-label="New password input"
          />
        </Input.Wrapper>

        <Input.Wrapper
          label="Confirm New Password"
          aria-label="Confirm new password input wrapper"
        >
          <PasswordInput
            value={passwordData.confirmPassword}
            onChange={(e) => {
              setPasswordData({
                ...passwordData,
                confirmPassword: e.target.value,
              });
            }}
            aria-label="Confirm new password input"
          />
        </Input.Wrapper>

        <Button
          onClick={handlePasswordUpdate}
          w="30%"
          mt="lg"
          aria-label="Update password button"
        >
          Update Password
        </Button>
      </Stack>

      <Divider w="70%" my="lg" aria-label="Section divider" />

      {/* 昼夜模式切换 */}
      <Title c="indigo" order={2} aria-label="Theme settings title">
        Theme Settings
      </Title>
      <Group w="100%" mt="sm" gap={"5%"} aria-label="Theme switch group">
        <Group gap="xs" aria-label="Theme switch inner group">
          <Switch
            onClick={toggleTheme}
            checked={colorScheme === "dark"}
            aria-label="Theme switch"
          />
          <Text aria-label="Dark mode label">Dark Mode</Text>
        </Group>
      </Group>
    </Stack>
  );
}
