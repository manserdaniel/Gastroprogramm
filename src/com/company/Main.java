package com.company;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Connection conn = null;
        Scanner scanner = new Scanner(System.in);
        Scanner scannerInt = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/gastroprogramm?user=root";

        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Database connected");

            Statement stmt = null;

            try {

                String sql = "";
                stmt = conn.createStatement();
                sql = "DELETE from ingredients";
                stmt.executeUpdate(sql);
                sql = "DELETE from ingredient";
                stmt.executeUpdate(sql);
                sql = "DELETE from food";
                stmt.executeUpdate(sql);

                ResultSet rs;

                System.out.println("Wie viele Speisen möchten Sie gerne eintragen?");
                int foods = scannerInt.nextInt();

                String[][] foodArray = new String[foods][2];

                for (int i = 0; i < foodArray.length; i++) {

                    int food_id = insertFoodIntoDB(conn, scanner, stmt, foodArray, i);

                    // ingredients
                    System.out.println("Wieviele Zutaten werden hierfür benötigt?");
                    int ingredients = scannerInt.nextInt();
                    int ingredient_id = 0;
                    String[] ingredientsArray = new String[ingredients];

                    for (int j = 1; j <= ingredientsArray.length; j++) {

                        System.out.println("Geben sie Zutat " + j + " ein:");
                        String nextIngredient = scanner.nextLine();

                        sql = "SELECT id FROM ingredient WHERE name = '" + nextIngredient + "';";
                        rs = stmt.executeQuery(sql);

                        // insert new ingredient
                        if (rs.next() == false) {
                            ingredient_id = insertIngredientIntoDB( stmt, food_id, ingredient_id,
                                                                    ingredientsArray, j, nextIngredient);
                        } else {
                            ingredient_id = rs.getInt(1);
                        }
                        insertIngredientToFoodTable(stmt, food_id, ingredient_id);
                    }
                }


            } catch (SQLException ex) {
                throw new Error("Problem ", ex);
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed");
                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void insertIngredientToFoodTable(Statement stmt, int food_id, int ingredient_id) throws SQLException {
        String sql;
        sql = "INSERT INTO ingredients (foodId, ingredientId)" + " values ('" + food_id + "','" + ingredient_id + "')";
        stmt.executeUpdate(sql);
    }

    private static int insertIngredientIntoDB(Statement stmt, int food_id, int ingredient_id, String[] ingredientsArray, int j, String nextIngredient) throws SQLException {
        String sql;
        ResultSet rs;
        sql = "INSERT INTO ingredient (name)" + " values ('" + nextIngredient + "')";
        stmt.executeUpdate(sql);
        ingredientsArray[j - 1] = nextIngredient;

        // get ingredient_id
        sql = "SELECT LAST_INSERT_ID();";
        rs = stmt.executeQuery(sql);

        if (rs.next()) {
            ingredient_id = rs.getInt("LAST_INSERT_ID()");
        }
        return ingredient_id;
    }

    private static int insertFoodIntoDB(Connection conn, Scanner scanner, Statement stmt, String[][] foodArray, int i) throws SQLException {
        String sql;
        ResultSet rs;// name of food
        System.out.println("Geben sie eine neue Speise ein:");
        String nextFood = scanner.nextLine();
        foodArray[i][0] = nextFood;

        // price of food
        System.out.println("Geben sie den Preis dieser Speise ein:");
        String price = scanner.nextLine();
        foodArray[i][1] = price;

        sql = "INSERT INTO food (name, price)" + " values (?, ?)";

        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setString(1, nextFood);
        preparedStmt.setString(2, price);
        preparedStmt.execute();

        // get food_id
        sql = "SELECT LAST_INSERT_ID();";
        rs = stmt.executeQuery(sql);
        int food_id = 0;

        if (rs.next()) {
            food_id = rs.getInt("LAST_INSERT_ID()");
        }
        return food_id;
    }
}
