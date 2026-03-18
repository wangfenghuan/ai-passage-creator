/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImageItem } from './ImageItem';
import type { OutlineItem } from './OutlineItem';
import type { TitleOption } from './TitleOption';
export type ArticleVO = {
    id?: number;
    taskId?: string;
    userId?: number;
    topic?: string;
    userDescription?: string;
    mainTitle?: string;
    subTitle?: string;
    titleOptions?: Array<TitleOption>;
    outline?: Array<OutlineItem>;
    content?: string;
    fullContent?: string;
    coverImage?: string;
    images?: Array<ImageItem>;
    status?: string;
    phase?: string;
    errorMessage?: string;
    createTime?: string;
    completedTime?: string;
};

