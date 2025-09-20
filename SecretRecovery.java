import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;

public class SecretRecovery {

    public static void main(String[] args) throws IOException {
        // Read JSON file into string
        String content = new String(Files.readAllBytes(Paths.get("input.json")));
        JSONObject obj = new JSONObject(content);

        // Extract n and k
        JSONObject keys = obj.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");
        
        // Read points (x, y) where y is in different bases
        List<Point> points = new ArrayList<>();

        // Loop through actual keys in JSON
        List<String> jsonKeys = new ArrayList<>();
        for (String key : obj.keySet()) {
            if (!key.equals("keys")) jsonKeys.add(key);
        }

        // Sort keys numerically
        jsonKeys.sort(Comparator.comparingInt(Integer::parseInt));

        for (String key : jsonKeys) {
            JSONObject point = obj.getJSONObject(key);
            int base = Integer.parseInt(point.getString("base"));
            String valueStr = point.getString("value");

            // Convert y from base to decimal (BigInteger)
            BigInteger y = new BigInteger(valueStr, base);
            BigInteger x = new BigInteger(key);
            points.add(new Point(x, y));
            
        }

        // Use first k points for interpolation
        List<Point> subset = points.subList(0, k);

        // Compute constant term using Lagrange interpolation at x=0
        BigInteger secret = lagrangeInterpolationAtZero(subset);
        System.out.println("\nSecret (c) = " + secret);
    }

    // Point class to hold x and y
    static class Point {
        BigInteger x, y;
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Lagrange interpolation at x=0
    static BigInteger lagrangeInterpolationAtZero(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i == j) continue;

                BigInteger xj = points.get(j).x;
                numerator = numerator.multiply(xj.negate());       // (0 - xj)
                denominator = denominator.multiply(xi.subtract(xj));
            }

            BigInteger li0 = numerator.divide(denominator); // Lagrange basis at 0
            result = result.add(yi.multiply(li0));
        }

        return result;
    }
}
