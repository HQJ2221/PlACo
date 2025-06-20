"use client";
import React, { useEffect, useState } from "react";
import {
  Title,
  TextInput,
  Button,
  Group,
  Alert,
  Loader,
  Notification,
} from "@mantine/core";
import { useRouter } from "next/navigation";
import { useForm } from "@mantine/form";

interface Authority {
  authority: string;
}

interface User {
  id: number;
  email: string;
  username: string;
  password: string;
  role: string;
  authorities: Authority[];
  enabled: boolean;
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
  accountNonLocked: boolean;
}

export const Settings = (props: { userId: number }) => {
  const router = useRouter();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [user, setUser] = useState<User | null>(null);

  const form = useForm({
    initialValues: {
      email: "",
      username: "",
      role: "",
    },

    validate: {
      email: (value) => (/^\S+@\S+$/.test(value) ? null : "Invalid email"),
      username: (value) => (value.length > 0 ? null : "Username is required"),
    },
  });

  useEffect(() => {
    const fetchUser = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          throw new Error("Failed to fetch user");
        }

        const data = await response.json();
        setUser(data.data);
        form.setValues({
          email: data?.data?.email,
          username: data?.data?.username,
          role: data?.data?.role,
        });
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [props.userId]);

  const handleUpdateUser = async (values: typeof form.values) => {
    setError(null);
    setSuccess(false);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(values),
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to update user");
      }

      setSuccess(true);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDeleteUser = async () => {
    setError(null);
    setSuccess(false);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/${props.userId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to delete user");
      }

      setSuccess(true);
      router.push("/dashboard/users");
    } catch (err: any) {
      setError(err.message);
    }
  };

  if (loading) {
    return <Loader size={30} />;
  }

  if (error) {
    return <Alert color="red">{error}</Alert>;
  }

  return (
    <div>
      <form onSubmit={form.onSubmit(handleUpdateUser)}>
        <TextInput label="Email" {...form.getInputProps("email")} />
        <TextInput label="Username" {...form.getInputProps("username")} />
        <TextInput label="Role" {...form.getInputProps("role")} />
        <Group mt="md">
          <Button type="submit">Update User</Button>
          <Button color="red" onClick={handleDeleteUser}>
            Delete User
          </Button>
        </Group>
      </form>
      {success && (
        <Notification color="green">User updated successfully!</Notification>
      )}
    </div>
  );
};
