# PlACo Frontend Developer Document

- [Installation & Build](#Installation-and-Build)

- [Brief Introduction](#Intro)
- [Development](#Development)
  - [Work Done](#Work-Done)
  - [Code Example](#Code-Example)
- [Formatting Rules](#Formatting-Rules)



## Installation and Build

1. Clone repo

```bash
git clone https://github.com/sustech-cs304/team-project-25spring-68.git -b frontend/main
```

2. Install packages and build dev

```bash
npm install && npm run dev
```

- the frontend runs on **PORT 3000** by default. If you want to connect to backend, configurate backend port in file `.env.local`. An example (if your backend server runs on port 8080):

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```



## Intro

- `PlACo` was construct under `next.js` framework
- We use `Mantine` for layout and component design, `lucide-react` for icon usage. (seen in `package.json`)
- For `Mantine` development, please refer to [Mantine](https://mantine.dev/).
- For `lucide` icon search, please refer to [Lucide](https://lucide.dev/).



## Development

### Work Done

> We briefly show what we've done. And you can learn how we build frontend by viewing our done work.

- **Normal File Structure:** In `next.js`, the strcture of file tree **correspond to** the page address.

  - For example, we have a module `/app/auth/login/page.tsx`, then when you run dev locally, this page is on `localhost:3000/auth/login`.

- **File Structure passed by params:** We have design about ID-specified object (e.g. courses, assignments, submissions, etc.). To route for a specific page, we should pass a param to build a page.

  - For example, our structore of file tree as follow:

  ```
  app/dashboard/courses
  |__[courseId]
  |  |__layout.tsx
  |  |__page.tsx
  |  |__...
  |__page.tsx
  ```

  - then when we load a course page with `courseId=1`, the address will be `localhost:3000/dashboard/courses/1`.
  - if there are multiple specific ID in the path, you should pass **all params** while design the page. (See [code example](#Code Example))

- **Layout & Page**: when we go to an internal module (e.g. `dashboard -> dashboard/courses/1`), module `layout.tsx` will render first, and kept as permanent layout when page goes "deeper" (e.g. `dashboard/courses/1 -> dashboard/courses/1/assignments/1`)

  - For example, we define a `Navbar` inside a course page (See `app/dashboard/courses/[courseId]/layout.tsx`). Even you enter an assignment in this course, the `Navbar` remains in page.
  - **Notice!** In `layout.tsx` (if exists), you must pass `children: React.ReactNode` as one of the params, and return it, so that `page.tsx` will be rendered. (See [code example](#Code Example))

### Code Example

> This part will help you quickly start. But for further development, please check `Next.Js`, `Mantine` Tutorial.

#### How to write a new page?

- Except for `app/layout.tsx`, which serve as `MantineProvider`(server), all other pages need to render in "client mode", so you need to add `"use client;"` at first.
- A page for rendering should be exported as a function. A component (of a page) written in another file should be exported as a const.

```tsx
"use client";
/* import packages */
export default function Page() {
  const [error, setError] = useState("");
  // ...
  
  return (
  	<div>{content}</div>
  );
}
```

- For a ID-specified page (like `courses/[courseId]/assignment/[assignmentId]`), pass path-params as `params`

```tsx
export default async function AssignmentPage({
  params,
}: {
  params: Promise<{courseId: number, assignmentId: number}>;
}) {
  const { courseId } = await params;
  //...
}
```

- If you want to use `layout.tsx` and `page.tsx` to build a new page with permanent layout (e.g. `courses/[courseId]`):

```tsx
// layout.tsx
export default function Layout({
  params,
  children,
}: {
  params: Promise<{ courseId: number }>;
  children: React.ReactNode;
}) {
  //...
  return (
  	<div>{children}</div>
  );
}
```

```tsx
// page.tsx
export default function CoursePage({
  params,
}: {
  params: Promise<{ courseId: number }>;
}) {
  // ...
  return (<div>{content}</div>);
}
```



## Formatting Rules

### Interface & global

- If you want to define some **global constants or variables**, write them in `app/global.tsx`.
- If you want to add a new **data structure** and reuse it in other module, write it in `app/interface.tsx`.

### Hooks

- You can customize new hooks despite existed hooks in `Mantine`, and place them into `app/hooks`

### Code Format

- Use **lowercase** and **line** to name files(modules). E.g. `assignment-client.tsx`
- Use **camel** to name functions (e.g. `fetchData()`), variable (e.g. `courseId`) and use-state constant (e.g. `const [filteredLog, setFilteredLog] = useState(null);`)

- Use **upper camel** to name interface/data structure (e.g. `UserCourse`)
- Use **uppercase** and **underline** to name global constant (e.g. `HEADER_HEIGHT`)
