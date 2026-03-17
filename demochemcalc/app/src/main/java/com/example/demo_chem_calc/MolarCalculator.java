package com.example.demo_chem_calc;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
//import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class MolarCalculator {

    public static CalculationResult calculateMolarMass(String formula) {
        try {
            // Создаём молекулярную формулу из строки
            IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(
                    formula, DefaultChemObjectBuilder.getInstance()
            );

            // Получаем молярную массу
            double mass = MolecularFormulaManipulator.getMass(mf);

            return new CalculationResult(true, formula, mass, "");

        } catch (Exception e) {
            return new CalculationResult(false, formula, 0, "Ошибка: Неверная химическая формула");
        }
    }

    // Класс для хранения результатов расчета
    public static class CalculationResult {
        private boolean success;
        private String formula;
        private double molarMass;
        private String errorMessage;

        public CalculationResult(boolean success, String formula, double molarMass, String errorMessage) {
            this.success = success;
            this.formula = formula;
            this.molarMass = molarMass;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getFormula() { return formula; }
        public double getMolarMass() { return molarMass; }
        public String getErrorMessage() { return errorMessage; }
    }
}



