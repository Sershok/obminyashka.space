import { useState } from 'react';
import * as yup from 'yup';
import { Formik } from 'formik';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

import api from 'REST/Resources';
import { GoogleSvg } from 'assets/icons';
import { getLang } from 'store/auth/slice';
import { route } from 'routes/routeConstants';
import CheckBox from 'components/common/checkbox';
import InputForAuth from 'components/common/input';
import { Button } from 'components/common/buttons';
import { getTranslatedText } from 'components/local/localization';
import {
  NO_SPACE,
  EMAIL_REG_EXP,
  PASSWORD_REG_EXP,
  PASSWORD_ALT_CODE_EXP,
  USERNAME_ALT_CODE_EXP,
} from 'config';

import { Extra, WrapperButton, Form } from '../sign-in/styles';

const SignUp = () => {
  const navigate = useNavigate();
  const lang = useSelector(getLang);
  const [loading, setLoading] = useState(false);
  const [checkbox, setCheckbox] = useState(false);

  const changeCheckBox = () => {
    setCheckbox((prev) => !prev);
  };

  const validationRegisterSchema = yup.object().shape({
    email: yup
      .string()
      .email(getTranslatedText('errors.invalidEmailFormat', lang))
      .required(getTranslatedText('errors.requireField', lang))
      .matches(EMAIL_REG_EXP, getTranslatedText('errors.max129', lang))
      .matches(NO_SPACE, getTranslatedText('errors.noSpace', lang))
      .default(() => ''),
    username: yup
      .string()
      .required(getTranslatedText('errors.requireField', lang))
      .min(2, getTranslatedText('errors.min2', lang))
      .max(50, getTranslatedText('errors.max50', lang))
      .matches(
        USERNAME_ALT_CODE_EXP,
        getTranslatedText('errors.altCodeMatch', lang)
      )
      .matches(NO_SPACE, getTranslatedText('errors.noSpace', lang))
      .default(() => ''),
    password: yup
      .string()
      .required(getTranslatedText('errors.requireField', lang))
      .min(8, getTranslatedText('errors.min8', lang))
      .max(30, getTranslatedText('errors.max30', lang))
      .matches(
        PASSWORD_REG_EXP,
        getTranslatedText('errors.passwordMatch', lang)
      )
      .matches(
        PASSWORD_ALT_CODE_EXP,
        getTranslatedText('errors.passwordMatch', lang)
      )
      .matches(NO_SPACE, getTranslatedText('errors.noSpace', lang))
      .default(() => ''),
    confirmPassword: yup
      .string()
      .oneOf(
        [yup.ref('password')],
        getTranslatedText('errors.passwordMismatch', lang)
      )
      .required(getTranslatedText('errors.requireField', lang))
      .default(() => ''),
  });
  const initialRegisterValues = validationRegisterSchema.cast({});

  return (
    <Form>
      <Formik
        validateOnBlur
        initialValues={initialRegisterValues}
        validationSchema={validationRegisterSchema}
        onSubmit={async (dataFormik, onSubmitProps) => {
          setLoading(true);
          try {
            await api.fetchAuth.postAuthRegister(dataFormik);
            setLoading(false);
            navigate(route.login);
          } catch (err) {
            setLoading(false);
            if (err.response.status === 409) {
              const { error: message } = err.response.data;
              onSubmitProps.setErrors({ username: message });
            }
          }
        }}
      >
        {({ errors, isSubmitting, handleSubmit, isValid, dirty }) => (
          <>
            <div>
              <InputForAuth
                name="email"
                type="email"
                text={getTranslatedText('auth.regEmail', lang)}
              />

              <InputForAuth
                type="text"
                name="username"
                text={getTranslatedText('auth.regLogin', lang)}
              />

              <InputForAuth
                name="password"
                type="password"
                text={getTranslatedText('auth.regPassword', lang)}
              />

              <InputForAuth
                type="password"
                name="confirmPassword"
                text={getTranslatedText('auth.regConfirm', lang)}
              />
            </div>

            <Extra>
              <CheckBox
                fontSize="14px"
                checked={checkbox}
                margin="0 0 44px 0"
                click={changeCheckBox}
                text={getTranslatedText('auth.agreement', lang)}
              />
            </Extra>

            <WrapperButton>
              <Button
                bold
                mb="44px"
                type="submit"
                width="222px"
                height="48px"
                lHeight="24px"
                isLoading={loading}
                text={getTranslatedText('auth.signUp', lang)}
                disabling={!checkbox || isSubmitting || (!dirty && !isValid)}
                click={!errors.email || !errors.username ? handleSubmit : null}
              />

              <Button
                bold
                mb="64px"
                height="48px"
                type="button"
                width="222px"
                lHeight="24px"
                icon={<GoogleSvg />}
                text={getTranslatedText('auth.signUp', lang)}
                click={() =>
                  window.location.assign('/oauth2/authorization/google')
                }
              />
            </WrapperButton>
          </>
        )}
      </Formik>
    </Form>
  );
};

export default SignUp;