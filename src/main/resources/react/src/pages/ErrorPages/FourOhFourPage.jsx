import { useNavigate } from 'react-router-dom';

import {
  loop,
  shadow,
  greenDots,
  fourOhFour,
  orangeDots,
  shadowDark,
} from 'assets/img/all_images_export/errorPage';
import { getTranslatedText } from 'components/local/localization';

import { goTo } from './helpers';
import * as Styles from './styles';

const FourOhFourPage = () => {
  const navigate = useNavigate();

  return (
    <>
      <Styles.WrapCenter>
        <Styles.Img404 src={fourOhFour} alt="404" />

        <Styles.WrapperShadow>
          <Styles.ImageLight src={shadow} alt="shadow" />
          <Styles.ImageDark src={shadowDark} alt="shadow dark" />
        </Styles.WrapperShadow>
      </Styles.WrapCenter>

      <Styles.WrapOImg>
        <Styles.Image src={orangeDots} alt="orange dots" />
      </Styles.WrapOImg>

      <Styles.WrapGImg>
        <Styles.Image src={greenDots} alt="green dots" />
      </Styles.WrapGImg>

      <Styles.WrapRImg>
        <Styles.Image src={loop} alt="loop" />
      </Styles.WrapRImg>

      <Styles.Tittle>{getTranslatedText('fourOhFour.noPage')}</Styles.Tittle>

      <Styles.WrapperButton>
        <Styles.MainButton onClick={() => goTo('home', navigate)}>
          {getTranslatedText('fourOhFour.mainPage')}
        </Styles.MainButton>

        <Styles.BackButton onClick={() => goTo('', navigate)}>
          {getTranslatedText('fourOhFour.backPage')}
        </Styles.BackButton>
      </Styles.WrapperButton>
    </>
  );
};
export default FourOhFourPage;
