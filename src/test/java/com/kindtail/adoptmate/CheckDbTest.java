package com.kindtail.adoptmate;

import org.junit.jupiter.api.Test;
import java.sql.*;

class CheckDbTest {

    @Test
    void checkAnimalsInRealDb() {
        String url = "jdbc:mysql://svc.sel3.cloudtype.app:31603/adoptpet_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        String user = "adoptmate";
        String pass = "adoptmate";

        System.out.println("\n========================================================");
        System.out.println("🔗 Cloudtype MySQL 데이터베이스 연결 시도 중...");
        System.out.println("========================================================");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ [DB 연결 성공!]");
            String sql = "SELECT animal_id, species, breed, color, gender, age, status, image, member_id, created_at FROM animal";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                int count = 0;
                System.out.println("\n---------------------------------------------------------------------------------------------------------");
                System.out.printf("| %-4s | %-6s | %-12s | %-6s | %-6s | %-4s | %-10s | %-20s |\n", 
                                  "ID", "종", "품종", "색상", "성별", "나이", "보호상태", "등록일시");
                System.out.println("---------------------------------------------------------------------------------------------------------");
                
                while (rs.next()) {
                    count++;
                    System.out.printf("| %-4d | %-6s | %-12s | %-6s | %-6s | %-4d | %-10s | %-20s |\n",
                            rs.getLong("animal_id"),
                            rs.getString("species"),
                            rs.getString("breed"),
                            rs.getString("color"),
                            rs.getString("gender"),
                            rs.getLong("age"),
                            rs.getString("status"),
                            rs.getString("created_at") != null ? rs.getString("created_at") : "-"
                    );
                    String img = rs.getString("image");
                    if (img != null && !img.isBlank()) {
                        System.out.printf("   ㄴ 📷 이미지 URL: %s\n", img);
                    }
                }
                
                if (count == 0) {
                    System.out.println("|  [안내] 현재 DB의 animal 테이블에 등록된 데이터가 0건입니다.                           |");
                }
                System.out.println("---------------------------------------------------------------------------------------------------------");
                System.out.println("📊 총 등록된 동물 수: " + count + "마리\n");
            }
        } catch (Exception e) {
            System.err.println("❌ [DB 조회 실패]: " + e.getMessage());
        }
    }
}
