import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",

  // /uploads/** 요청을 Spring Boot 백엔드로 프록시
  async rewrites() {
    return [
      {
        source: '/uploads/:path*',
        destination: `${process.env.BACKEND_URL || 'http://localhost:8080'}/uploads/:path*`,
      },
    ];
  },
};

export default nextConfig;
