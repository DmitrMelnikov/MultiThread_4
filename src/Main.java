import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static BlockingQueue<String> queueForCharA = new ArrayBlockingQueue<String>(100, true);
    private static BlockingQueue<String> queueForCharB = new ArrayBlockingQueue<String>(100, true);
    private static BlockingQueue<String> queueForCharC = new ArrayBlockingQueue<String>(100, true);

    private static Map<Character, String> mapResult = new ConcurrentHashMap<Character, String>();

    private static AtomicInteger countA = new AtomicInteger(0);
    private static AtomicInteger countB = new AtomicInteger(0);
    private static AtomicInteger countC = new AtomicInteger(0);
    private static final int numberOfItems = 100_000;
    private static final int numberOfTexts = 10_000;

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static int searchLetter(String letters, char letter) {
        int count = 0;
        for (int i = 0; i < letters.length(); i++) {
            if (letter == letters.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    public static void threadBody(char letter, BlockingQueue<String> queue, AtomicInteger count) {
        for (int i = 0; i < numberOfTexts; i++) {
            try {
                String text = queue.take();
                int numberInText = searchLetter(text, letter);
                if (count.get() < numberInText) {
                    count.set(numberInText);
                    if (!mapResult.containsKey(letter)) {
                        mapResult.put(letter, text);
                    } else {
                        for (Map.Entry<Character, String> entry : mapResult.entrySet()) {
                            if (letter == entry.getKey()) {
                                entry.setValue(text);
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        System.out.println("Начало работы...");
        Thread first = new Thread(() -> {
            for (int i = 0; i < numberOfTexts; i++) {
                try {
                    String text = generateText("abc", numberOfItems);
                    queueForCharA.put(text);
                    queueForCharB.put(text);
                    queueForCharC.put(text);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        first.start();

        Thread threadA = new Thread(() -> {
            threadBody('a', queueForCharA, countA);
        });
        threadA.start();

        Thread threadB = new Thread(() -> {
            threadBody('b', queueForCharB, countB);
        });
        threadB.start();

        Thread threadC = new Thread(() -> {
            threadBody('c', queueForCharC, countC);
        });
        threadC.start();

        first.join();
        threadC.join();
        threadA.join();
        threadB.join();

        for (Map.Entry<Character, String> entry : mapResult.entrySet()) {
            System.out.println("Символ    : " + entry.getKey());
            System.out.println("Строка    : " + entry.getValue().substring(99950) + "...");
            System.out.println("Вхождений : " + searchLetter(entry.getValue(), entry.getKey()));
        }
        System.out.println("Окончание работы...");
    }
}
