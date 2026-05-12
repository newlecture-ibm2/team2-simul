import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",

  // Proxy specific paths to Spring Boot backend (docker-compose only exposes frontend)
  async rewrites() {
    const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";
    return [
      {
        source: "/uploads/:path*",
        destination: `${backendUrl}/uploads/:path*`,
      },
      // Swagger/OpenAPI
      {
        source: "/api-docs/:path*",
        destination: `${backendUrl}/api-docs/:path*`,
      },
      {
        source: "/v3/api-docs/:path*",
        destination: `${backendUrl}/v3/api-docs/:path*`,
      },
      {
        source: "/swagger-ui.html",
        destination: `${backendUrl}/swagger-ui.html`,
      },
      {
        source: "/swagger-ui/:path*",
        destination: `${backendUrl}/swagger-ui/:path*`,
      },
    ];
  },
};

export default nextConfig;
