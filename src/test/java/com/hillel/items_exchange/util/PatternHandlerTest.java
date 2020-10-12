package com.hillel.items_exchange.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hillel.items_exchange.util.PatternHandler.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternHandlerTest {

    @ParameterizedTest
    @MethodSource("createCorrectEmails")
    void testEmailRegexp_whenEmailIsCorrect(String correctEmail) {
        assertTrue(correctEmail.matches(EMAIL));
    }

    @ParameterizedTest
    @MethodSource("createWrongEmails")
    void testEmailRegexp_whenEmailIsWrong(String wrongEmail) {
        assertFalse(wrongEmail.matches(EMAIL));
    }

    @ParameterizedTest
    @MethodSource("createCorrectPasswords")
    void testPasswordRegexp_whenPasswordsCorrect(String correctPassword) {
        assertTrue(correctPassword.matches(PASSWORD));
    }

    @ParameterizedTest
    @MethodSource("createWrongPasswords")
    void testPasswordRegexp_whenPasswordsWrong(String wrongPassword) {
        assertFalse(wrongPassword.matches(PASSWORD));
    }

    @ParameterizedTest
    @MethodSource("createCorrectPhoneNumbers")
    void testPhoneNumberRegexp_whenPhoneNumberIsCorrect(String correctPhoneNumber) {
        assertTrue(correctPhoneNumber.matches(PHONE_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("createWrongPhoneNumbers")
    void testPhoneNumberRegexp_whenPhoneNumberIsWrong(String wrongPhoneNumber) {
        assertFalse(wrongPhoneNumber.matches(PHONE_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("createCorrectUserNames")
    void testUsernameRegexp_whenUsernameIsCorrect(String correctUserName) {
        assertTrue(correctUserName.matches(USERNAME));
    }

    @ParameterizedTest
    @MethodSource("createWrongUserNames")
    void testUsernameRegexp_whenUsernameIsWrong(String wrongName) {
        assertFalse(wrongName.matches(USERNAME));
    }

    @ParameterizedTest
    @MethodSource("createCorrectWordsEmptyOrMin2Max50")
    void testWordEmptyOrMin2Max50_whenWordIsCorrect(String correctWord) {
        assertTrue(correctWord.matches(WORD_EMPTY_OR_MIN_2_MAX_50));
    }

    @ParameterizedTest
    @MethodSource("createWrongWordsEmptyOrMin2Max50")
    void testWordEmptyOrMin2Max50_whenWordIsWrong(String wrongWord) {
        assertFalse(wrongWord.matches(WORD_EMPTY_OR_MIN_2_MAX_50));
    }

    private static List<String> createCorrectEmails() {
        List<String> correctEmails = new ArrayList<>();
        correctEmails.add("pushkin@ukr.net");
        correctEmails.add("pushkin.145@gmail.com");
        correctEmails.add("pushkin@mail.ru");
        correctEmails.add("pushkin.pushkin@mail.ru");
        correctEmails.add("pushkin.pushkin@mail.mail.ru");
        correctEmails.add("1@ukr.net");
        correctEmails.add("1@111.ru");

        return correctEmails;
    }

    private static List<String> createWrongEmails() {
        List<String> wrongEmails = new ArrayList<>();
        wrongEmails.add("pushkin@ukr.n");
        wrongEmails.add(".145@gmail.com");
        wrongEmails.add("pushkin@@mail.ru");
        wrongEmails.add("1@111.11");
        wrongEmails.add("pushkin.11");
        wrongEmails.add("pushkin.1u");
        wrongEmails.add("pushkin.u1");
        wrongEmails.add("1@111.1u");
        wrongEmails.add("1@111.1ru");
        wrongEmails.add("1@111.ru1");
        wrongEmails.add("1@ma~il.ru");

        List<Character> correctCharacters = List.of('.', '_', '+', '-');

        wrongEmails.addAll(getStringsWithSpecialSymbol("pu", "shkin@ukr.net", correctCharacters));
        wrongEmails.addAll(getStringsWithRussianOrUkrCharacter("pu", "shkin@ukr.net"));

        return wrongEmails;
    }

    private static List<String> createCorrectPasswords() {
        List<String> correctPasswords = new ArrayList<>();
        correctPasswords.add("aA1");
        correctPasswords.add("1aA");
        correctPasswords.add("a1A");
        correctPasswords.add("#aA1");
        correctPasswords.add("aA1@");
        correctPasswords.add("~aA1`");
        correctPasswords.add("~`aA1`!@#$%^&*()_+-=");

        correctPasswords.addAll(getStringsWithSpecialSymbol("a", "A1", Collections.emptyList()));

        return correctPasswords;
    }

    private static List<String> createWrongPasswords() {
        List<String> wrongPasswords = new ArrayList<>();
        wrongPasswords.add("");
        wrongPasswords.add("a");
        wrongPasswords.add("1");
        wrongPasswords.add("A");
        wrongPasswords.add("a1");
        wrongPasswords.add("aA");
        wrongPasswords.add("A1");
        wrongPasswords.add("aa1");
        wrongPasswords.add("a11");
        wrongPasswords.add("AA1");

        wrongPasswords.add(" aA1");
        wrongPasswords.add("aA1 ");
        wrongPasswords.add(" aA1 ");
        wrongPasswords.add("a A1");

        wrongPasswords.add("a`1");
        wrongPasswords.add("A`1");
        wrongPasswords.add("1#A");
        wrongPasswords.add("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");

        wrongPasswords.addAll(getStringsWithRussianOrUkrCharacter("a", "A1"));

        return wrongPasswords;
    }

    private static List<String> createCorrectPhoneNumbers() {
        List<String> correctPhoneNumbers = new ArrayList<>();
        correctPhoneNumbers.add("+38-050-223-32-23");
        correctPhoneNumbers.add("38-050-223-32-23");
        correctPhoneNumbers.add("     38  -  050  -  223  -  32  -  23     ");
        correctPhoneNumbers.add("     38-050  -223-32-23     ");
        correctPhoneNumbers.add("     38-050-223-   32-23     ");

        correctPhoneNumbers.add("+38.050.223.32.23");
        correctPhoneNumbers.add("38.050.223.32.23");
        correctPhoneNumbers.add("     38.050.223.32.23     ");

        correctPhoneNumbers.add("+38-(050)-223-32-23");
        correctPhoneNumbers.add("+38-(050-223-32-23");
        correctPhoneNumbers.add("+38-050)-223-32-23");

        correctPhoneNumbers.add("+38-050.223-32-23");
        correctPhoneNumbers.add("+38-050-223.32-23");
        correctPhoneNumbers.add("+38-050-223-32.23");
        correctPhoneNumbers.add("+38-050.223.32.23");

        correctPhoneNumbers.add("+38.050.223-32-23");
        correctPhoneNumbers.add("+38.050-223.32-23");
        correctPhoneNumbers.add("+38.050-223-32.23");
        correctPhoneNumbers.add("+38.050-223-32-23");

        correctPhoneNumbers.add("+00-000-000-00-00");

        return correctPhoneNumbers;
    }

    private static List<String> createWrongPhoneNumbers() {
        List<String> wrongPhoneNumbers = new ArrayList<>();
        wrongPhoneNumbers.add("+3-050-223-32-23");
        wrongPhoneNumbers.add("+38-50-223-32-23");
        wrongPhoneNumbers.add("+38-050-22-32-23");
        wrongPhoneNumbers.add("+38-050-223-3-23");
        wrongPhoneNumbers.add("+38-050-223-32-2");
        wrongPhoneNumbers.add("+38-)050-223-32-23");
        wrongPhoneNumbers.add("+38-050(-223-32-23");
        wrongPhoneNumbers.add("+38-)050(-223-32-23");
        wrongPhoneNumbers.add("+38-050-(223)-32-23");
        wrongPhoneNumbers.add("+38-050-(223)-(32)-(23)");

        wrongPhoneNumbers.add("    +  38-050-223-32-23     ");
        wrongPhoneNumbers.add("    ++  38-050-223-32-23     ");
        wrongPhoneNumbers.add("    +  3  8-050-223-32-23     ");
        wrongPhoneNumbers.add("    +  38-0  50-223-32-23     ");
        wrongPhoneNumbers.add("    +  38-050-22  3-32-23     ");

        wrongPhoneNumbers.add("+38-05a-223-32-23");
        wrongPhoneNumbers.add("+38-05O-223-32-23");
        wrongPhoneNumbers.add("+38-05Ы-223-32-23");
        wrongPhoneNumbers.add("+38-05ё-223-32-23");
        wrongPhoneNumbers.add("+38-050-22W-32-23");
        wrongPhoneNumbers.add("+38-050-223-3w-23");
        wrongPhoneNumbers.add("+38-050-22Й-32-23");
        wrongPhoneNumbers.add("+38-050-223-3щ-23");

        List<Character> correctCharacters = List.of('.', '-');
        wrongPhoneNumbers.addAll(getStringsWithSpecialSymbol("+38-050-223", "32-23", correctCharacters));
        wrongPhoneNumbers.addAll(getStringsWithSpecialSymbol("+38.050.223", "32.23", correctCharacters));
        wrongPhoneNumbers.addAll(getStringsWithSpecialSymbol("+38-050-223-", "2-23", correctCharacters));
        wrongPhoneNumbers.addAll(getStringsWithSpecialSymbol("+38.050.223.", "2.23", correctCharacters));

        return wrongPhoneNumbers;
    }

    private static List<String> createCorrectUserNames() {
        List<String> correctNames = new ArrayList<>();
        correctNames.add("pushkin");
        correctNames.add("ThisNameHasLengthMoreThan100SymbolsThisNameHasLengthMoreThan100SymbolsThisNameHasLengthMoreThan100Symbols");
        correctNames.add("~~``~~");
        correctNames.add("`_'");
        correctNames.add("'@'\"`!");
        correctNames.add("1");
        correctNames.add("$a");
        correctNames.add("$");
        correctNames.add("a$");
        correctNames.add("$+a");
        correctNames.add("a+$");

        correctNames.addAll(getStringsWithSpecialSymbol("pu", "shkin", Collections.emptyList()));
        correctNames.addAll(getStringsWithRussianOrUkrCharacter("aaa", "bbb"));

        return correctNames;
    }

    private static List<String> createWrongUserNames() {
        List<String> wrongNames = new ArrayList<>();
        wrongNames.add("");
        wrongNames.add(" ");
        wrongNames.add("aaa bbb");
        wrongNames.add(" aaa");
        wrongNames.add("aaa ");

        return wrongNames;
    }

    private static List<String> createCorrectWordsEmptyOrMin2Max50() {
        List<String> correctWords = new ArrayList<>();
        correctWords.add("");
        correctWords.add("aA");
        correctWords.add("11");
        correctWords.add("aA1");
        correctWords.add("aA1-");
        correctWords.add("aA1'");
        correctWords.add("aA1'a");
        correctWords.add("aA1-a");
        correctWords.add("LengthIs50LengthIs50LengthIs50LengthIs50LengthIs50");

        correctWords.addAll(getStringsWithRussianOrUkrCharacter("aa", "bb"));

        return correctWords;
    }

    private static List<String> createWrongWordsEmptyOrMin2Max50() {
        List<String> wrongWords = new ArrayList<>();
        wrongWords.add("a");
        wrongWords.add("1");
        wrongWords.add("ThisWordHasLengthMoreThan50CharactersItsLengthIs51S");

        List<Character> correctCharacters = List.of('\'', '_', '`', '-');
        wrongWords.addAll(getStringsWithSpecialSymbol("aaa", "bbb", correctCharacters));

        return wrongWords;
    }

    private static List<String> getStringsWithSpecialSymbol(String str1, String str2, List<Character> deletedCharacters) {
        List<Character> addedCharacters = new ArrayList<>(List.of(
                '`', '~', '!', '@', '#', '$', '%', '^', '&', '*',
                '(', ')', '+', '=', '[', ']', '{', '}', '\'', '"', ':', ';', '/', '\\', '|', '?', '.', ',', '<', '>',
                '-', '_'));
        addedCharacters.removeAll(deletedCharacters);

        return addedCharacters.stream()
                .map(character -> str1.concat(String.valueOf(character)).concat(str2))
                .collect(Collectors.toList());
    }

    private static List<String> getStringsWithRussianOrUkrCharacter(String str1, String str2) {
        List<Character> russianChars = List.of('Й', 'й', 'Ц', 'ц', 'У', 'у', 'Г', 'г', 'Ш', 'ш', 'Щ', 'щ', 'З', 'з',
                'Х', 'х', 'Ъ', 'ъ', 'Ф', 'ф', 'Ы', 'ы', 'П', 'п', 'Л', 'л', 'Д', 'д', 'Ж', 'ж', 'Э', 'э', 'Я', 'я',
                'Ч', 'ч', 'М', 'м', 'Ь', 'ь', 'Б', 'б', 'Ю', 'ю', 'Ґ', 'Є', 'І', 'Ї', 'і', 'є', 'ї', 'ґ');

        return russianChars.stream()
                .map(character -> str1.concat(String.valueOf(character)).concat(str2))
                .collect(Collectors.toList());
    }
}