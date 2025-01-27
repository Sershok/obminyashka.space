import styled, { css } from 'styled-components';

export const styleSet = css`
  padding: 38px 22px 38px 25px;
  width: 290px;
  border: 2px dashed ${({ theme }) => theme.colors.btnBlue};
  border-radius: 20px;
`;

export const BlockStyleSet = css`
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
`;

export const CategoryFilter = styled.div`
  ${styleSet};
`;

export const Filter = styled.div`
  ${styleSet};
  margin-top: 20px;
`;

export const SelectBlock = styled.div`
  ${BlockStyleSet};
`;

export const CheckBoxBlock = styled.div`
  ${BlockStyleSet};
`;

export const Title = styled.div`
  font-weight: 700;
  font-size: 19px;
  line-height: 24px;
  text-transform: uppercase;
  color: ${({ theme }) => theme.colors.btnBlue};
`;

export const TitleOfEachCategory = styled.div`
  font-size: 18px;
  line-height: 24px;
`;

export const Close = styled.div`
  display: flex;
  flex-shrink: 0;

  ${({ theme, isSelected }) => css`
    opacity: ${isSelected ? 1 : 0};

    svg {
      path {
        fill: ${theme.colors.white};
      }
    }
  `}
`;

export const ScrollBar = styled.div`
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 425px;
  overflow: auto;

  ::-webkit-scrollbar {
    width: 8px;
    height: 10px;
  }
`;
