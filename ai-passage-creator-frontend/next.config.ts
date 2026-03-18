import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://127.0.0.1:8567/api/:path*',
      },
      {
        source: '/v3/api-docs/:path*',
        destination: 'http://127.0.0.1:8567/v3/api-docs/:path*',
      }
    ];
  },
};

export default nextConfig;
