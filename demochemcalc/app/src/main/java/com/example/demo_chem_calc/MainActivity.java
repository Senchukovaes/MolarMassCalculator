package com.example.demo_chem_calc;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText formulaInput;
    private Button calculateButton;
    private Button clearButton;
    private TextView resultText;

    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Настройка отступов для системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Цвет статус-бара под шапку приложения
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_bar_teal));

        // Инициализация UI элементов
        initViews();

        // Настройка обработчиков событий
        setupClickListeners();

        setupChemicalKeyboard();

        // База данных
        repository = new Repository(this);
        repository.checkAndCopyDatabase();


    }

    private Button calcButton;
    private Button flaskButton;
    private Button dropButton;

    private void initViews() {
        formulaInput = findViewById(R.id.formulaInput);
        calculateButton = findViewById(R.id.calculateButton);
        clearButton = findViewById(R.id.clearButton);
        resultText = findViewById(R.id.resultText);
        calcButton = findViewById(R.id.calcButton);
        flaskButton = findViewById(R.id.flaskButton);
        dropButton = findViewById(R.id.dropButton);

        setupModeTabs();
    }

    private void setupModeTabs() {
        calcButton.setSelected(true);
        calcButton.setOnClickListener(v -> selectTab(calcButton));
        flaskButton.setOnClickListener(v -> selectTab(flaskButton));
        dropButton.setOnClickListener(v -> selectTab(dropButton));
    }

    private void selectTab(Button selected) {
        calcButton.setSelected(selected == calcButton);
        flaskButton.setSelected(selected == flaskButton);
        dropButton.setSelected(selected == dropButton);
    }

    private void setupClickListeners() {
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateMolarMass();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });
    }

    private void calculateMolarMass() {
        String input = formulaInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Введите химическую формулу", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, input - название вещества?
        String formulaFromDb = repository.getFormulaByName(input);

        String formula;

        if (formulaFromDb != null) {
            // Нашли вещество по названию
            formula = formulaFromDb;
        } else {
            // Введённый текст - формула
            formula = input;
        }

        String normalized = normalizeFormula(formula);

        // Проверяем валидность элементов
        if (!containsOnlyRealElements(normalized)) {
            resultText.setText("Неверная химическая формула");
            return;
        }

        // Выполняем расчет
        MolarCalculator.CalculationResult result =
                MolarCalculator.calculateMolarMass(normalized);

        // Отображаем результат
        if (result.isSuccess()) {
            String resultString = String.format(
                    "Формула: %s\nМолярная масса: %.2f г/моль",
                    formatFormulaForOutput(result.getFormula()),
//                    result.getFormula(),
//                    formula,
                    result.getMolarMass()
            );
            resultText.setText(resultString);
        } else {
            resultText.setText(result.getErrorMessage());
            Toast.makeText(this, "Ошибка расчета", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAll() {
        formulaInput.setText("");
        resultText.setText("Результат будет тут");
        formulaInput.requestFocus();
    }

    private int currentPage = 1;

    private void setupChemicalKeyboard() {
        loadKeyboardPage(R.layout.keyboard_page1);
    }

    private void loadKeyboardPage(int layoutId) {
        FrameLayout container = findViewById(R.id.keyboardContainer);
        container.removeAllViews();
        View page = getLayoutInflater().inflate(layoutId, container, false);
        container.addView(page);

        // Находим корневой элемент
        View keyboardView = findKeyboardRoot(page, layoutId);

        if (keyboardView != null) {
            // Рекурсивно добавляем обработчики для всех кнопок
            setupAllButtonListeners(keyboardView);
        } else {
            // Если не нашли по ID, используем саму страницу как корень
            setupAllButtonListeners(page);
        }
    }

    private View findKeyboardRoot(View page, int layoutId) {
        // Возвращаем корневой элемент клавиатуры в зависимости от страницы
        if (layoutId == R.layout.keyboard_page1) {
            return page.findViewById(R.id.chemKeyboard1);
        } else if (layoutId == R.layout.keyboard_page2) {
            return page.findViewById(R.id.chemKeyboard2);
        } else if (layoutId == R.layout.keyboard_page3) {
            return page.findViewById(R.id.chemKeyboard3);
        } else if (layoutId == R.layout.keyboard_page4) {
            return page.findViewById(R.id.chemKeyboard4);
        } else if (layoutId == R.layout.keyboard_page5) {
            return page.findViewById(R.id.chemKeyboard5);
        } else if (layoutId == R.layout.keyboard_page6) {
            return page.findViewById(R.id.chemKeyboard6);
        } else if (layoutId == R.layout.keyboard_page7) {
            return page.findViewById(R.id.chemKeyboard7);
        }
        return null;
    }

    private void setupAllButtonListeners(View view) {
        if (view instanceof LinearLayout) {
            // Если это LinearLayout, обрабатываем всех его детей
            LinearLayout layout = (LinearLayout) view;
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                setupAllButtonListeners(child); // Рекурсивный вызов
            }
        } else if (view instanceof Button) {
            // Если это кнопка, добавляем обработчик
            Button button = (Button) view;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Определяем по id кнопки, а не по тексту
                    int buttonId = button.getId();

                    if (buttonId == R.id.nextPageBtn) {
                        // Переход на следующую страницу с текущей
                        if (currentPage == 1) {
                            currentPage = 2;
                            loadKeyboardPage(R.layout.keyboard_page2);
                        } else if (currentPage == 2) {
                            currentPage = 3;
                            loadKeyboardPage(R.layout.keyboard_page3);
                        } else if (currentPage == 3) {
                            currentPage = 4;
                            loadKeyboardPage(R.layout.keyboard_page4);
                        } else if (currentPage == 4) {
                            currentPage = 5;
                            loadKeyboardPage(R.layout.keyboard_page5);
                        } else if (currentPage == 5) {
                            currentPage = 6;
                            loadKeyboardPage(R.layout.keyboard_page6);
                        } else if (currentPage == 6) {
                            currentPage = 7;
                            loadKeyboardPage(R.layout.keyboard_page7);
                        }




                    } else if (buttonId == R.id.previousPageBtn) {
                        // Переход на предыдущую страницу с текущей
                        if (currentPage == 2) {
                            currentPage = 1;
                            loadKeyboardPage(R.layout.keyboard_page1);
                        } else if (currentPage == 3) {
                            currentPage = 2;
                            loadKeyboardPage(R.layout.keyboard_page2);
                        } else if (currentPage == 4) {
                            currentPage = 3;
                            loadKeyboardPage(R.layout.keyboard_page3);
                        } else if (currentPage == 5) {
                            currentPage = 4;
                            loadKeyboardPage(R.layout.keyboard_page4);
                        }  else if (currentPage == 6) {
                            currentPage = 5;
                            loadKeyboardPage(R.layout.keyboard_page5);
                        }  else if (currentPage == 7) {
                            currentPage = 6;
                            loadKeyboardPage(R.layout.keyboard_page6);
                        }
                    } else if (buttonId == R.id.firstPageBtn) {
                        // Прямой переход на первую страницу
                        currentPage = 1;
                        loadKeyboardPage(R.layout.keyboard_page1);

                    } else if (buttonId == R.id.deleteBtn) {
                        String current = formulaInput.getText().toString();
                        if (!current.isEmpty()) {
                            formulaInput.setText(current.substring(0, current.length() - 1));
                            formulaInput.setSelection(formulaInput.getText().length());
                        }
                    } else {
                        // Это химическая кнопка - обрабатываем текст
                        String text = button.getText().toString();
                        onKeyboardButtonClick(text);
                    }
                }
            });
        }
    }


    private void onKeyboardButtonClick(String text) {
        if (text.equals("Clear")) {
            formulaInput.setText("");
            return;
        }

        if (text.equals("Del")) {
            String current = formulaInput.getText().toString();
            if (!current.isEmpty()) {
                formulaInput.setText(current.substring(0, current.length() - 1));
                formulaInput.setSelection(formulaInput.getText().length());
            }
            return;
        }

        int start = Math.max(formulaInput.getSelectionStart(), 0);
        int end = Math.max(formulaInput.getSelectionEnd(), 0);
        formulaInput.getText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());
    }

    public static String normalizeFormula(String raw) {
        if (raw == null) return "";

        String s = raw.trim();

        // убираем пробелы, включая NBSP и zero-width
        s = s.replaceAll("\\s+", "");

        // Удаляем zero-width и подобные невидимые символы
        s = s.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");

        // Замены юникод подскриптов 
        s = s.replace('₀', '0');
        s = s.replace('₁', '1');
        s = s.replace('₂', '2');
        s = s.replace('₃', '3');
        s = s.replace('₄', '4');
        s = s.replace('₅', '5');
        s = s.replace('₆', '6');
        s = s.replace('₇', '7');
        s = s.replace('₈', '8');
        s = s.replace('₉', '9');

        // Надстрочные цифры 
        s = s.replace('⁰', '0');
        s = s.replace('¹', '1');
        s = s.replace('²', '2');
        s = s.replace('³', '3');
        s = s.replace('⁴', '4');
        s = s.replace('⁵', '5');
        s = s.replace('⁶', '6');
        s = s.replace('⁷', '7');
        s = s.replace('⁸', '8');
        s = s.replace('⁹', '9');

        // Замены похожих символов на безопасные
        s = s.replace('·', '.');
        s = s.replace('•', '.');
        s = s.replace('*', '.');
        s = s.replace('[', '(');
        s = s.replace(']', ')');

        return s;
    }

    // Превращаем индексы в маленькие цифры
    private static final char[] SMALL_DIGITS = {
            '₀','₁','₂','₃','₄','₅','₆','₇','₈','₉'
    };

    private String formatFormulaForOutput(String formula) {
        if (formula == null || formula.isEmpty()) return formula;

        StringBuilder result = new StringBuilder();
        char[] arr = formula.toCharArray();

        boolean insideElementOrBracket = false;
        // Была буква или закрывающая скобка, значит дальше может быть индекс

        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];

            // Если буква, значит следующее число потенциально индекс
            if (Character.isLetter(c)) {
                insideElementOrBracket = true;
                result.append(c);
                continue;
            }

            // Если скобка ) — перед индексом, как в (OH)2
            if (c == ')') {
                insideElementOrBracket = true;
                result.append(c);
                continue;
            }

            // Если скобка ( — дальше ожидается элемент, но индекс не должен идти сразу
            if (c == '(') {
                insideElementOrBracket = false;
                result.append(c);
                continue;
            }

            // Если это цифра
            if (Character.isDigit(c)) {

                // Если первый символ формулы -> коэффициент, НЕ индекс
                if (i == 0) {
                    result.append(c);
                    insideElementOrBracket = false;
                    continue;
                }

                // Если перед цифрой стоит *, · или другая цифра -> коэффициент
                char prev = arr[i - 1];

                if (prev == '*' || prev == '·' || Character.isDigit(prev)) {
                    result.append(c);
                    insideElementOrBracket = false;
                    continue;
                }

                // Если перед цифрой буква или скобка, это индекс
                if (insideElementOrBracket) {
                    result.append(SMALL_DIGITS[c - '0']);
                    continue;
                }

                // Иначе – обычное число
                result.append(c);
                insideElementOrBracket = false;
                continue;
            }

            // Любой другой символ
            insideElementOrBracket = false;
            result.append(c);
        }

        return result.toString();
    }

    // Проверка, что формула содержит только реальные элементы
    private boolean containsOnlyRealElements(String formula) {

        // Все существующие элементы (1–118)
        String[] elements = {
                "H","He","Li","Be","B","C","N","O","F","Ne",
                "Na","Mg","Al","Si","P","S","Cl","Ar",
                "K","Ca","Sc","Ti","V","Cr","Mn","Fe","Co","Ni",
                "Cu","Zn","Ga","Ge","As","Se","Br","Kr",
                "Rb","Sr","Y","Zr","Nb","Mo","Tc","Ru","Rh","Pd",
                "Ag","Cd","In","Sn","Sb","Te","I","Xe",
                "Cs","Ba","La","Ce","Pr","Nd","Pm","Sm","Eu","Gd",
                "Tb","Dy","Ho","Er","Tm","Yb","Lu",
                "Hf","Ta","W","Re","Os","Ir","Pt","Au","Hg","Tl",
                "Pb","Bi","Po","At","Rn",
                "Fr","Ra","Ac","Th","Pa","U","Np","Pu","Am","Cm",
                "Bk","Cf","Es","Fm","Md","No","Lr",
                "Rf","Db","Sg","Bh","Hs","Mt","Ds","Rg","Cn","Fl","Lv",
                "Ts","Og"
        };

        // Создаём для быстрого поиска
        java.util.Set<String> known = new java.util.HashSet<>();
        for (String el : elements) known.add(el);

        // Парсим формулу: ищем элементы вида A или Ab
        for (int i = 0; i < formula.length(); ) {
            char c = formula.charAt(i);

            if (Character.isUpperCase(c)) {
                // Собираем элемент: заглавная + возможная строчная
                String element;
                if (i + 1 < formula.length() && Character.isLowerCase(formula.charAt(i + 1))) {
                    element = "" + c + formula.charAt(i + 1);
                    i += 2;
                } else {
                    element = "" + c;
                    i += 1;
                }

                // Проверяем, существует ли элемент
                if (!known.contains(element)) return false;

            } else {
                // Всё остальное просто пропускаем (цифры, скобки, точки)
                i++;
            }
        }

        return true;
    }





}