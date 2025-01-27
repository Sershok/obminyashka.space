import { useContext, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Title,
  showMessage,
  ProductCard,
  PagePagination,
} from 'obminyashka-components';

import api from 'REST/Resources';
import { route } from 'routes/routeConstants';
import { getCity } from 'Utils/getLocationProperties';
import { Filtration, SearchContext } from 'components/common';
import { getTranslatedText } from 'components/local/localization';

import * as Styles from './styles';

const SearchResults = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { search, setSearch, isFetch, setIsFetch } = useContext(SearchContext);
  const [adv, setAdv] = useState({});

  const searchResults = search || searchParams.get('search');

  const getAdv = async (page) => {
    const currentPage = page ?? 1;
    setSearchParams({ search: searchResults });

    try {
      const response = await api.search.getSearch(
        searchResults,
        currentPage - 1
      );
      setAdv(response);
    } catch (err) {
      if (err?.response?.status !== 404) {
        showMessage.error(err.response?.data ?? err.message);
      }
    } finally {
      setIsFetch(false);
    }
  };

  useEffect(() => {
    if (isFetch && searchResults) {
      getAdv();
    }
  }, [isFetch, searchResults]);

  useEffect(() => {
    if (searchResults || search) {
      getAdv();
    } else {
      setSearch(searchResults);
    }

    return () => {
      setSearch('');
    };
  }, []);

  const moveToProductPage = (id) => {
    navigate(route.productPage.replace(':id', id));
  };

  return (
    <Styles.SearchingResults>
      <Styles.SearchingContent>
        <Styles.FilterContainer>
          <Styles.BreadCrumbs>
            {getTranslatedText('filterPage.home')}/
            <Styles.Span>
              {getTranslatedText('filterPage.searchResults')}
            </Styles.Span>
          </Styles.BreadCrumbs>

          <Filtration />
        </Styles.FilterContainer>

        <div>
          <Title text={getTranslatedText('filterPage.searchResults')} />

          {adv.content && (
            <PagePagination
              onChange={getAdv}
              current={adv.number + 1}
              pageSize={adv?.size || 1}
              total={adv.totalElements}
            >
              {adv.content?.length > 0 &&
                adv.content.map((item) => (
                  <ProductCard
                    text={item.title}
                    key={item.advertisementId}
                    city={getCity(item.location)}
                    buttonText={getTranslatedText('button.look')}
                    picture={`data:image/jpeg;base64,${item.image}`}
                    onClick={() => moveToProductPage(item.advertisementId)}
                  />
                ))}
            </PagePagination>
          )}
        </div>
      </Styles.SearchingContent>
    </Styles.SearchingResults>
  );
};

export { SearchResults };
