package com.hillel.items_exchange.controller;

import com.hillel.items_exchange.exception.ElementsNumberExceedException;
import com.hillel.items_exchange.exception.UnsupportedMediaTypeException;
import com.hillel.items_exchange.model.Image;
import com.hillel.items_exchange.model.Product;
import com.hillel.items_exchange.service.ImageService;
import com.hillel.items_exchange.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProductService productService;
    @MockBean
    private ImageService imageService;
    @Mock
    private Product product;
    @Captor
    private ArgumentCaptor<List<byte[]>> listArgumentCaptor;
    private ArrayList<Image> testImages;
    private MockMultipartFile jpeg;

    @BeforeEach
    void setUp() throws IOException, UnsupportedMediaTypeException {
        jpeg = new MockMultipartFile("files", "image-jpeg.jpeg", MediaType.IMAGE_JPEG_VALUE, "image jpeg".getBytes());
        mocksInit();
    }

    private void mocksInit() throws IOException, UnsupportedMediaTypeException {
        when(productService.findById(1L)).thenReturn(Optional.of(product));
        testImages = IntStream.range(0, 10)
                .collect(ArrayList::new, (images, value) -> images.add(new Image()), ArrayList::addAll);
        when(product.getImages()).thenReturn(testImages);
        when(imageService.compress(List.of(jpeg))).thenReturn(List.of(jpeg.getBytes()));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldThrowExceptionWhenTotalAmountMoreThan10() throws Exception {
        MvcResult mvcResult = mockMvc.perform(multipart("/image/{product_id}", 1L)
                .file(jpeg)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(productService).findById(anyLong());
        assertThat(mvcResult.getResolvedException(), is(instanceOf(ElementsNumberExceedException.class)));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldSaveImagesWhenTotalAmountLessThan10() throws Exception {
        testImages.remove(0);

        mockMvc.perform(multipart("/image/{product_id}", 1L)
                .file(jpeg)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(imageService).compress(anyList());
        verify(imageService).saveToProduct(any(), listArgumentCaptor.capture());
        assertEquals(jpeg.getBytes(), listArgumentCaptor.getValue().get(0));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldReturn400WhenProductIsNotExist() throws Exception {
        mockMvc.perform(multipart("/image/{product_id}", 50L)
                .file(jpeg))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}