FROM node:20-alpine AS builder
WORKDIR /app

# 패키지 정보 복사 및 의존성 설치
COPY package.json package-lock.json* ./
RUN npm ci

# 빌드 환경 변수 주입 (Next.js 빌드 시점)
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_KAKAO_CLIENT_ID
ARG NEXT_PUBLIC_NAVER_CLIENT_ID
ARG NEXT_PUBLIC_GOOGLE_CLIENT_ID
ARG BACKEND_URL

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_KAKAO_CLIENT_ID=$NEXT_PUBLIC_KAKAO_CLIENT_ID
ENV NEXT_PUBLIC_NAVER_CLIENT_ID=$NEXT_PUBLIC_NAVER_CLIENT_ID
ENV NEXT_PUBLIC_GOOGLE_CLIENT_ID=$NEXT_PUBLIC_GOOGLE_CLIENT_ID
ENV BACKEND_URL=$BACKEND_URL

# 소스 코드 복사 및 빌드
COPY . .
RUN npm run build

# 실행 환경
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV production

# Next.js standalone 빌드 결과물 복사 
# (주의: next.config.ts/js 파일에 output: 'standalone' 설정이 필요합니다)
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]
