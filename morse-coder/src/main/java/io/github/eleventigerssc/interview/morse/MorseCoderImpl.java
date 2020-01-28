package io.github.eleventigerssc.interview.morse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class MorseCoderImpl implements MorseCoder {
    private static final String STRING_SPLIT_EMPTY = "";

    private static final String MORSE_WORDS_SEPARATOR = "     ";
    private static final String MORSE_LETTER_SEPARATOR = " ";

    private static final Map<Character, String> morseEncode = new HashMap<>();
    private static final Map<String, Character> morseDecode = new HashMap<>();
    private static final String SENTENCE_END = ".";
    private static final char CHAR_WORD_SEPARATOR = ' ';

    static {
        initMorseEncodeVocabulary();
        initMorseDecodeVocabulary();
    }

    private static void initMorseEncodeVocabulary() {
        morseEncode.put('a', ".-");
        morseEncode.put('b', "-...");
        morseEncode.put('c', "-.-.");
        morseEncode.put('d', "-..");
        morseEncode.put('e', ".");
        morseEncode.put('f', "..-.");
        morseEncode.put('g', "--.");
        morseEncode.put('h', "....");
        morseEncode.put('i', "..");
        morseEncode.put('j', ".---");
        morseEncode.put('k', "-.-");
        morseEncode.put('l', ".-..");
        morseEncode.put('m', "--");
        morseEncode.put('n', "-.");
        morseEncode.put('o', "---");
        morseEncode.put('p', ".--.");
        morseEncode.put('q', "--.-");
        morseEncode.put('r', ".-.");
        morseEncode.put('s', "...");
        morseEncode.put('t', "-");
        morseEncode.put('u', "..-");
        morseEncode.put('v', "...-");
        morseEncode.put('w', ".--");
        morseEncode.put('x', "-..-");
        morseEncode.put('y', "-.--");
        morseEncode.put('z', "--..");
        morseEncode.put('1', ".----");
        morseEncode.put('2', "..---");
        morseEncode.put('3', "...--");
        morseEncode.put('4', "....-");
        morseEncode.put('5', ".....");
        morseEncode.put('6', "-....");
        morseEncode.put('7', "--...");
        morseEncode.put('8', "---..");
        morseEncode.put('9', "----.");
        morseEncode.put('0', "-----");
        morseEncode.put('?', "··--··");
        morseEncode.put(',', "--··--");
        morseEncode.put('.', "·-·-·-");
    }

    private static void initMorseDecodeVocabulary() {
        morseDecode.put(".-", 'a');
        morseDecode.put("-...", 'b');
        morseDecode.put("-.-.", 'c');
        morseDecode.put("-..", 'd');
        morseDecode.put(".", 'e');
        morseDecode.put("..-.", 'f');
        morseDecode.put("--.", 'g');
        morseDecode.put("....", 'h');
        morseDecode.put("..", 'i');
        morseDecode.put(".---", 'j');
        morseDecode.put("-.-", 'k');
        morseDecode.put(".-..", 'l');
        morseDecode.put("--", 'm');
        morseDecode.put("-.", 'n');
        morseDecode.put("---", 'o');
        morseDecode.put(".--.", 'p');
        morseDecode.put("--.-", 'q');
        morseDecode.put(".-.", 'r');
        morseDecode.put("...", 's');
        morseDecode.put("-", 't');
        morseDecode.put("..-", 'u');
        morseDecode.put("...-", 'v');
        morseDecode.put(".--", 'w');
        morseDecode.put("-..-", 'x');
        morseDecode.put("-.--", 'y');
        morseDecode.put("--..", 'z');
        morseDecode.put(".----", '1');
        morseDecode.put("..---", '2');
        morseDecode.put("...--", '3');
        morseDecode.put("....-", '4');
        morseDecode.put(".....", '5');
        morseDecode.put("-....", '6');
        morseDecode.put("--...", '7');
        morseDecode.put("---..", '8');
        morseDecode.put("----.", '9');
        morseDecode.put("-----", '0');
        morseDecode.put("··--··", '?');
        morseDecode.put("--··--", ',');
        morseDecode.put("·-·-·-", '.');
    }

    @Override
    public Flowable<String> decode(InputStream inputStream) {
        return Flowable.create(flowableEmitter -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (flowableEmitter.isCancelled()) return;

                String[] morseWords = line.split(MORSE_WORDS_SEPARATOR);
                for (int i = 0; i < morseWords.length; i++) {
                    String word = morseWords[i];
                    if (word.equals(STRING_SPLIT_EMPTY)) {
                        flowableEmitter.onNext(MORSE_LETTER_SEPARATOR);
                        continue;
                    }

                    StringBuilder resultWordBuilder = new StringBuilder();
                    String[] morseLetters = word.split(MORSE_LETTER_SEPARATOR);
                    for (String letter : morseLetters) {
                        Character character = morseDecode.get(letter);
                        if (character != null) {
                            resultWordBuilder.append(character);
                        }
                    }
                    String resultWord = resultWordBuilder.toString().toUpperCase();
                    flowableEmitter.onNext(resultWord);
                    if (i != morseWords.length - 1 && !resultWord.endsWith(SENTENCE_END)) {
                        flowableEmitter.onNext(MORSE_LETTER_SEPARATOR);
                    }
                }
            }
            flowableEmitter.onComplete();
        }, BackpressureStrategy.MISSING);
    }

    @Override
    public Flowable<String> encode(InputStream inputStream) {
        return Flowable.create(flowableEmitter -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (flowableEmitter.isCancelled()) return;

                char[] chars = line.toLowerCase().toCharArray();
                StringBuilder wordBuilder = new StringBuilder();
                for (int i = 0; i < chars.length; i++) {
                    char character = chars[i];
                    if (character == CHAR_WORD_SEPARATOR) {
                        removeLastAppended(wordBuilder);
                        flowableEmitter.onNext(wordBuilder.toString());
                        flowableEmitter.onNext(MORSE_WORDS_SEPARATOR);
                        wordBuilder = new StringBuilder();
                    }

                    String morseLetter = morseEncode.get(character);
                    if (morseLetter != null) {
                        wordBuilder.append(morseLetter);
                        wordBuilder.append(MORSE_LETTER_SEPARATOR);
                    }

                    if (i == chars.length - 1 && wordBuilder.length() > 0) {
                        removeLastAppended(wordBuilder);
                        flowableEmitter.onNext(wordBuilder.toString());
                    }
                }
            }
            flowableEmitter.onComplete();
        }, BackpressureStrategy.MISSING);
    }

    private void removeLastAppended(StringBuilder wordBuilder) {
        if (wordBuilder.length() != 0) {
            wordBuilder.delete(wordBuilder.length() - 1, wordBuilder.length());
        }
    }
}
