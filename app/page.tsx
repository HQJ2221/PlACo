import Image from "next/image";

export default function Home() {
  return (
    <div
      className="flex flex-col items-center justify-center min-h-screen p-8 gap-8"
      aria-label="Main Page container"
      role="main"
    >
      <Image
        src="/Logo.png"
        alt="Logo"
        width={500}
        height={500}
        priority
        aria-label="Logo image"
        role="img"
      />
      <a
        className="rounded-full border border-solid border-black/[.08] dark:border-white/[.145] transition-colors flex items-center justify-center hover:bg-[#f2f2f2] dark:hover:bg-[#1a1a1a] hover:border-transparent font-medium text-base h-12 px-5"
        href="./auth/login"
        rel="noopener noreferrer"
        aria-label="Go to Login page"
        role="link"
      >
        <span aria-label="Go Login button text" role="text">
          Go Login
        </span>
      </a>
    </div>
  );
}
