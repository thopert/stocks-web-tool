package at.uibk.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;

@FacesValidator(value = "at.uibk.validator.BigDecimalGreaterZeroValidator")
public class BigDecimalGreaterZeroValidator implements Validator<BigDecimal> {
    @Override
    public void validate(FacesContext context, UIComponent component, BigDecimal number) throws ValidatorException {
        if(number == null)
            return;

        if (number.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zahl > 0 erforderlich!",
                    null));
    }
}
