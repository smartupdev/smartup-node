package global.smartup.node.compoment;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class Validator {

    @Autowired
    private LocalValidatorFactoryBean validatorFactoryBean;

    public String validate(Object obj, Class... classes) {
        Map<String, String> errors = validateAll(obj, classes);
        if (errors.isEmpty()) {
            return null;
        }
        String k = errors.keySet().iterator().next();
        return errors.get(k);
    }

    public Map<String, String> validateAll(Object obj, Class... classes) {
        HashMap<String, String> ret = new HashMap<>();
        Set<ConstraintViolation<Object>> set = validatorFactoryBean.getValidator().validate(obj, classes);
        if (set != null && set.size() > 0) {
            for (ConstraintViolation<Object> violation : set) {
                String error = violation.getMessage();
                String field = "";
                Path path = violation.getPropertyPath();
                Iterator<Path.Node> iterator = path.iterator();
                while (iterator.hasNext()) {
                    Path.Node node = iterator.next();
                    if (StringUtils.isNotBlank(node.getName())) {
                        field = node.getName();
                        break;
                    }
                }
                ret.put(field, error);
            }
        }
        return ret;
    }

}

